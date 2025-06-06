package wraith.fwaystones.integration.journeymap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.wispforest.endec.*;
import io.wispforest.endec.format.gson.GsonDeserializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import joptsimple.internal.Strings;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.client.option.BooleanOption;
import journeymap.api.v2.client.option.OptionCategory;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneymapPlugin implements IClientPlugin {

    private final List<UUID> queuedWaypoints;
    // API reference
    private IClientAPI api = null;
    private BooleanOption enabled;
    private BooleanOption displayWaypoints;
    //    private BooleanOption randomizeColor;
    private boolean mappingStarted = false;
    // Map of waypoints' ids to their corresponding hash. HASH -> WAYPOINT_ID
    private final Map<UUID, String> uuidToWaypointId = new HashMap<>();

    private static final Endec<Map<UUID, String>> ENDEC = Endec.map(UUID::toString, UUID::fromString, Endec.STRING);

    private static final StructEndec<Map<UUID, String>> ADDED_WAYSTONES = new StructEndec<>() {
        @Override
        public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, Map<UUID, String> value) {
            struct.field("added_waystones", ctx, ENDEC, value);
        }

        @Override
        public Map<UUID, String> decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
            return struct.field("added_waystones", ctx, ENDEC);
        }
    };

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    /**
     * Do not manually instantiate this class, Journeymap will take care of it.
     */
    public JourneymapPlugin() {
        this.queuedWaypoints = new ArrayList<>();
    }

    /**
     * This is called very early in the mod loading life cycle if Journeymap is loaded.
     *
     * @param api Client API implementation
     */
    @Override
    public void initialize(@NotNull IClientAPI api) {
        this.api = api;

        // event registration
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe(getModId(), event -> {
            OptionCategory category = new OptionCategory(getModId(), "fwaystones.integration.journeymap.category");
            this.enabled = new BooleanOption(category, "enabled", "fwaystones.integration.journeymap.enable", true);
            this.displayWaypoints = new BooleanOption(category, "displayed", "fwaystones.integration.journeymap.waypoints_enable", true);
        });

        ClientEventRegistry.MAPPING_EVENT.subscribe(getModId(), event -> {
            if (event.getStage().equals(MappingEvent.Stage.MAPPING_STARTED)) {
                mappingStarted = true;

                var dataFile = api.getDataPath("saved_waypoints.json");

                if (dataFile != null) {
                    try {
                        if (dataFile.exists()) dataFile.createNewFile();

                        var str = Files.readString(dataFile.toPath());

                        var json = GSON.fromJson(str, JsonElement.class);

                        var savedWaypoints = ADDED_WAYSTONES.decodeFully(GsonDeserializer::of, json);

                        this.uuidToWaypointId.putAll(savedWaypoints);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                buildQueuedWaypoints();
            } else {
                var dataFile = api.getDataPath("saved_waypoints.json");

                if (dataFile != null) {
                    try {
                        var json = ADDED_WAYSTONES.encodeFully(GsonSerializer::of, this.uuidToWaypointId);

                        Files.writeString(dataFile.toPath(), json.toString(), StandardOpenOption.CREATE);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                mappingStarted = false;
                api.removeAll(getModId());
            }
        });

        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(getModId(), this::onFullscreenAddonButton);

        WaystoneEvents.ON_WAYSTONE_DISCOVERY.register((player, uuid, position) -> this.onDiscover(uuid));
        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.register((player, uuid, position) -> this.onRemove(uuid));
        WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.register((player, uuids) -> {
            api.removeAll(getModId());
        });
        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            if (type.equals(DataChangeType.REMOVAL)) {
                this.onRemove(uuid);
            } else if (type.equals(DataChangeType.NAME) || type.equals(DataChangeType.COLOR)) {
                this.onDataChange(uuid);
            }
        });
    }

    /**
     * Adds a themeable button in the fullscreen map to toggle waystone waypoint display.
     *
     * @param addonButtonDisplayEvent - the event
     */
    private void onFullscreenAddonButton(
        FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {
        addonButtonDisplayEvent.getThemeButtonDisplay()
            .addThemeToggleButton(
                "fwaystones.integration.journeymap.theme.on",
                "fwaystones.integration.journeymap.theme.off",
                Identifier.of(FabricWaystones.MOD_ID, "fabric_waystones_icon.png"),
                displayWaypoints.get(),
                b -> {
                    b.toggle();
                    displayWaypoints.set(b.getToggled());
                    updateWaypointDisplay(b.getToggled());
                });
    }

    @Override
    public String getModId() {
        return FabricWaystones.MOD_ID;
    }

    private void updateWaypointDisplay(boolean display) {
        if (!display) {
            api.removeAll(getModId());
        } else {

            getClientValidDiscovered().forEach(this::addWaypoint);
        }
    }

    private void buildQueuedWaypoints() {
        queuedWaypoints.forEach(this::addWaypoint);
        queuedWaypoints.clear();
    }

    private void onRemove(UUID uuid) {
        if (!enabled.get()) return;

        var waypoint = api.getWaypoint(getModId(), uuidToWaypointId.get(uuid));
        if (waypoint != null) {
            api.removeWaypoint(getModId(), waypoint);
            uuidToWaypointId.remove(uuid);
        }
    }

    private void onDiscover(UUID uuid) {
        var storage = getStorage();
        if (storage == null || !enabled.get()) return;
        if (mappingStarted) {
            addWaypoint(uuid);
        } else {
            // queue waypoints to be displayed once mapping has started.
            // FabricWaystones that are sent on server join is too early to be displayed, so we need to wait.
            queuedWaypoints.add(uuid);
        }
    }

    private void onDataChange(UUID uuid) {
        onRemove(uuid);
        addWaypoint(uuid);
    }

    private void addWaypoint(UUID uuid) {
        var waypointId = uuidToWaypointId.get(uuid);
        var group = this.api.getWaypointGroupByName(FabricWaystones.MOD_ID, "Waystones");
        if (group == null) {
            group = WaypointFactory.createWaypointGroup(FabricWaystones.MOD_ID, "Waystones"); // might want to add this to i18n
            group.setLocked(true); // so users cannot move waypoints in and out of the group(can still delete the group, but will be recreated on game join.)
        }

        var storage = getStorage();

        if (storage == null || api.getWaypoint(getModId(), waypointId) != null) return; // do not recreate waypoint

        var waystone = storage.getData(uuid);
        var position = storage.getPosition(uuid);
        if (waystone == null || position == null) return;

        var name = waystone.nameAsString();

        var waypoint = WaypointFactory.createClientWaypoint(
            getModId(),
            position.blockPos(),
            Strings.isNullOrEmpty(name) ? "Unnamed Waystone" : name,
            position.worldName(),
            false
        );

        waypoint.setIconResourceLoctaion(Identifier.of(FabricWaystones.MOD_ID, "images/fabric_waystones_icon.png"));

        uuidToWaypointId.put(waystone.uuid(), waypoint.getGuid());

        try {
            waypoint.setColor(waystone.color());
            waypoint.setIconColor(null); // so icon is not colored
            waypoint.setEnabled(displayWaypoints.get());
            this.api.addWaypoint(getModId(), waypoint);
            group.setLocked(false); // needed for now until JM is updated, cannot add waypoints to locked groups in code currently. So we need to unlock it and lock it.
            group.addWaypoint(waypoint);
            group.setLocked(true);
        } catch (Throwable t) {
            FabricWaystones.LOGGER.error(t.getMessage(), t);
        }
    }

    //--

    @Nullable
    private static WaystoneDataStorage getStorage() {
        return WaystoneDataStorage.getStorage(MinecraftClient.getInstance());
    }

    @Nullable
    private static WaystonePlayerData getPlayerData() {
        var player = MinecraftClient.getInstance().player;

        if (player == null) return null;

        return WaystonePlayerData.getData(player);
    }

    public Collection<UUID> getClientValidDiscovered() {
        var data = getPlayerData();
        var storage = getStorage();

        if (data != null && storage != null) {
            var discoveredWaystones = data.discoveredWaystones();

            return discoveredWaystones.stream()
                    .filter(uuid -> storage.getPosition(uuid) != null)
                    .toList();
        }

        return Collections.emptyList();
    }
}
