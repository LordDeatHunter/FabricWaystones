package wraith.fwaystones.integration.journeymap;

import joptsimple.internal.Strings;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.client.option.BooleanOption;
import journeymap.api.v2.client.option.OptionCategory;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.integration.event.WaystoneEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneymapPlugin implements IClientPlugin {

    private final List<String> queuedWaypoints;
    // API reference
    private IClientAPI api = null;
    private BooleanOption enabled;
    private BooleanOption displayWaypoints;
    //    private BooleanOption randomizeColor;
    private boolean mappingStarted = false;
    // Map of waypoints' ids to their corresponding hash. HASH -> WAYPOINT_ID
    private final Map<String, String> waypointHashes = new HashMap<>();

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
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT_EVENT.subscribe(getModId(), event -> {
            OptionCategory category = new OptionCategory(getModId(), "fwaystones.integration.journeymap.category");
            this.enabled = new BooleanOption(category, "enabled", "fwaystones.integration.journeymap.enable", true);
            this.displayWaypoints = new BooleanOption(category, "displayed", "fwaystones.integration.journeymap.enable", true);
        });

        ClientEventRegistry.MAPPING_EVENT.subscribe(getModId(), event -> {
            if (event.getStage().equals(MappingEvent.Stage.MAPPING_STARTED)) {
                mappingStarted = true;
                buildQueuedWaypoints();
            } else {
                mappingStarted = false;
                api.removeAll(getModId());
            }
        });

        ClientEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(getModId(), this::onFullscreenAddonButton);

        WaystoneEvents.REMOVE_WAYSTONE_EVENT.register(this::onRemove);
        WaystoneEvents.DISCOVER_WAYSTONE_EVENT.register(this::onDiscover);
        WaystoneEvents.RENAME_WAYSTONE_EVENT.register(this::onRename);
        WaystoneEvents.FORGET_ALL_WAYSTONES_EVENT.register(player -> api.removeAll(getModId()));
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
                Identifier.of("fabric_waystones_icon"), // required to be in assets/journeymap/flat/icon due to themes
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
            FabricWaystones.WAYSTONE_STORAGE.getAllHashes().forEach(this::addWaypoint);
        }
    }

    private void buildQueuedWaypoints() {
        queuedWaypoints.forEach(this::addWaypoint);
        queuedWaypoints.clear();
    }

    private void onRemove(final String hash) {
        if (!enabled.get()) {
            return;
        }
        var waypoint = api.getWaypoint(getModId(), waypointHashes.get(hash));
        if (waypoint != null) {
            api.removeWaypoint(getModId(), waypoint);
        }
    }

    private void onDiscover(String hash) {
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return;
        }
        if (!enabled.get()) return;
        if (mappingStarted) {
            addWaypoint(hash);
        } else {
            // queue waypoints to be displayed once mapping has started.
            // FabricWaystones that are sent on server join is too early to be displayed, so we need to wait.
            queuedWaypoints.add(hash);
        }
    }

    private void onRename(String hash) {
        onRemove(hash);
        addWaypoint(hash);
    }

    private void addWaypoint(String hash) {
        var waypointId = waypointHashes.get(hash);
        if (FabricWaystones.WAYSTONE_STORAGE == null || api.getWaypoint(getModId(), waypointId) != null) {
            return; // do not recreate waypoint
        }
        var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            return;
        }

        var waypoint = WaypointFactory.createClientWaypoint(
            getModId(),
            waystone.way_getPos(),
            Strings.isNullOrEmpty(waystone.getWaystoneName()) ? "Unnamed Waystone" : waystone.getWaystoneName(),
            waystone.getWorldName(),
            false
        );

        waypoint.setIconResourceLoctaion(Identifier.of("images/fabric_waystones_icon.png"));

        waypointHashes.put(hash, waypoint.getId());

        try {
            waypoint.setColor(waystone.getColor());
            waypoint.setEnabled(displayWaypoints.get());
            this.api.addWaypoint(getModId(), waypoint);
        } catch (Throwable t) {
            FabricWaystones.LOGGER.error(t.getMessage(), t);
        }
    }
}
