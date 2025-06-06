package wraith.fwaystones.api;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.endec.impl.StructField;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.SyncWaystonePlayerDataChange;
import wraith.fwaystones.networking.packets.s2c.SyncWaystonePlayerData;
import wraith.fwaystones.util.SearchType;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WaystonePlayerData {

    private static final StructField<WaystonePlayerData, Set<WaystonePosition>> LEGACY_POSITIONS =
            optionalFieldOf(WaystonePosition.DEPRECATED_ENDEC.setOf(), "discovered_waystones", data -> Set.of(), Set::of);


    private static final WaystonePlayerDataKey<Set<UUID>> DISCOVERED_WAYSTONES_KEY = new WaystonePlayerDataKey<>("discovered_waystones_ids", collectionOf(BuiltInEndecs.UUID, ConcurrentHashMap::newKeySet), WaystonePlayerData::discoveredWaystones);
    private static final WaystonePlayerDataKey<Boolean> VIEW_DISCOVERED_WAYSTONES_KEY = new WaystonePlayerDataKey<>("view_discovered_waystones", Endec.BOOLEAN, WaystonePlayerData::viewDiscoveredWaystones);
    private static final WaystonePlayerDataKey<Boolean> VIEW_GLOBAL_WAYSTONES_KEY = new WaystonePlayerDataKey<>("view_global_waystones", Endec.BOOLEAN, WaystonePlayerData::viewGlobalWaystones);
    private static final WaystonePlayerDataKey<Boolean> AUTOFOCUS_WAYSTONE_FIELDS_KEY = new WaystonePlayerDataKey<>("autofocus_waystone_fields", Endec.BOOLEAN, WaystonePlayerData::autofocusWaystoneFields);
    private static final WaystonePlayerDataKey<Integer> TELEPORT_COOLDOWN_KEY = new WaystonePlayerDataKey<>("teleport_cooldown", Endec.INT, WaystonePlayerData::teleportCooldown);
    private static final WaystonePlayerDataKey<SearchType> WAYSTONE_SEARCH_TYPE_KEY = new WaystonePlayerDataKey<>("waystone_search_type",
            Endec.forEnum(SearchType.class).catchErrors((ctx, serializer, exception) -> {
                if (!(exception instanceof IllegalArgumentException e)) throw new RuntimeException(exception);

                FabricWaystones.LOGGER.warn("Received invalid waystone search type: ", e);

                return SearchType.CONTAINS;
            }),
            WaystonePlayerData::waystoneSearchType);

    public static final StructEndec<WaystonePlayerData> ENDEC = StructEndecBuilder.of(
            DISCOVERED_WAYSTONES_KEY.optionalFieldOf(ConcurrentHashMap::newKeySet),
            VIEW_DISCOVERED_WAYSTONES_KEY.optionalFieldOf(true),
            VIEW_GLOBAL_WAYSTONES_KEY.optionalFieldOf(true),
            AUTOFOCUS_WAYSTONE_FIELDS_KEY.optionalFieldOf(true),
            WAYSTONE_SEARCH_TYPE_KEY.optionalFieldOf(SearchType.CONTAINS),
            TELEPORT_COOLDOWN_KEY.optionalFieldOf(0),
            LEGACY_POSITIONS,
            (discoveredWaystones, viewDiscovered, viewGlobal, autofocusWaystoneFields,  searchType, cooldown, legacyPositions) -> {
                return new WaystonePlayerData(discoveredWaystones, viewDiscovered, viewGlobal, autofocusWaystoneFields, searchType, cooldown).setLegacyPositions(legacyPositions);
            }
    );

    private static final AttachmentType<WaystonePlayerData> ATTACHMENT_TYPE = AttachmentRegistry.<WaystonePlayerData>builder()
            .persistent(CodecUtils.toCodec(WaystonePlayerData.ENDEC))
            .copyOnDeath()
            .initializer(WaystonePlayerData::new)
            .buildAndRegister(FabricWaystones.id("player_data"));

    private final Set<WaystonePosition> legacyDiscoveredWaystones = new HashSet<>();

    private final Set<UUID> discoveredWaystones;

//    private final Map<WaystoneHash, OverrideData> discoveredWaystonesOverrides;
//    private final Map<String, WaystoneHash> discoveredGroups;

    private boolean viewDiscoveredWaystones;
    private boolean viewGlobalWaystones;
    private boolean autofocusWaystoneFields;
    private int teleportCooldown;
    private SearchType waystoneSearchType;

    private PlayerEntity player;

    WaystonePlayerData() {
        this(ConcurrentHashMap.newKeySet(), true, true, true, SearchType.CONTAINS, 0);
    }

    private WaystonePlayerData(Set<UUID> discoveredWaystones, boolean viewDiscoveredWaystones, boolean viewGlobalWaystones, boolean autofocusWaystoneFields, SearchType waystoneSearchType, int teleportCooldown) {
        this.discoveredWaystones = discoveredWaystones;
        this.viewDiscoveredWaystones = viewDiscoveredWaystones;
        this.viewGlobalWaystones = viewGlobalWaystones;
        this.autofocusWaystoneFields = autofocusWaystoneFields;
        this.waystoneSearchType = waystoneSearchType;
        this.teleportCooldown = teleportCooldown;
    }

    public WaystonePlayerData setLegacyPositions(Set<WaystonePosition> positions) {
        if (!positions.isEmpty()) {
            this.legacyDiscoveredWaystones.addAll(positions);
        } else {
            fixedHashes = true;
        }

        return this;
    }

    //--

    private boolean fixedHashes = false;

    private void convertFromLegacy() {
        var safeDiscoveredWaystones = new HashSet<WaystonePosition>();

        for (var discoveredWaystone : legacyDiscoveredWaystones) {
            var position = discoveredWaystone.attemptToFix(player.getScoreboard());

            if (!position.isUnsafe()) {
                safeDiscoveredWaystones.add(position);
            } else {
                FabricWaystones.LOGGER.error("Unable to fix a old Hash as it was not found within the storage for the given player: [Player: {}, Hash: {}]", player.getName(), position.hashString());
            }
        }

        this.legacyDiscoveredWaystones.clear();

        var data = WaystoneDataStorage.getStorage(player.getScoreboard());

        for (var position : safeDiscoveredWaystones) {
            var uuid = data.getUUID(position);

            if (uuid == null) {
                FabricWaystones.LOGGER.error("Unable to find a old Hash as it was not found within the storage for the given player: [Player: {}, Hash: {}]", player.getName(), position.toString());
            }

            this.discoveredWaystones.add(uuid);
        }

        this.fixedHashes = true;
    }

    public static WaystonePlayerData getData(PlayerEntity player) {
        var data = player.getAttachedOrCreate(ATTACHMENT_TYPE);

        if (player != data.player) {
            data.player = player;
        }

        if (!data.fixedHashes) {
            data.convertFromLegacy();
        }

        return data;
    }

    @ApiStatus.Internal
    public static void setData(PlayerEntity player, NbtCompound data) {
        player.setAttached(ATTACHMENT_TYPE, WaystonePlayerData.ENDEC.decodeFully(NbtDeserializer::of, data));
    }

    @ApiStatus.Internal
    public static void setData(PlayerEntity player, WaystonePlayerData data) {
        player.setAttached(ATTACHMENT_TYPE, data);
        WaystoneEvents.ON_PLAYER_WAYSTONE_DATA_UPDATE.invoker().onChange(player);
    }

    @ApiStatus.Internal
    public <T> void setDataUnsafe(WaystonePlayerDataKey<T> key, Object object) {
        var sentFromServer = player.getWorld().isClient();

        if (key.equals(VIEW_DISCOVERED_WAYSTONES_KEY)) {
            this.viewDiscoveredWaystones = (boolean) object;
        } else if (key.equals(VIEW_GLOBAL_WAYSTONES_KEY)) {
            this.viewGlobalWaystones = (boolean) object;
        } else if (key.equals(AUTOFOCUS_WAYSTONE_FIELDS_KEY)) {
            this.autofocusWaystoneFields = (boolean) object;
        } else if (key.equals(WAYSTONE_SEARCH_TYPE_KEY)) {
            this.waystoneSearchType = (SearchType) object;
        } else if (key.equals(TELEPORT_COOLDOWN_KEY) && sentFromServer) {
            this.teleportCooldown = (int) object;
        } else if (key.equals(DISCOVERED_WAYSTONES_KEY)) {
            var waystones = (Collection<UUID>) object;

            var storage = WaystoneDataStorage.getStorage(player);

            if (sentFromServer) {
                this.discoveredWaystones.clear();
                this.discoveredWaystones.addAll(waystones);

                for (var uuid : waystones) {
                    WaystoneEvents.ON_WAYSTONE_DISCOVERY.invoker().onDiscovery(this.player, uuid, storage.getPosition(uuid));
                }
            } else {
                var forgottenWaystones = this.discoveredWaystones.stream()
                        .filter(s -> !waystones.contains(s))
                        .collect(Collectors.toSet());

                this.forgetWaystones(forgottenWaystones, false);

                if (forgottenWaystones.size() == discoveredWaystones.size()) {
                    WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.invoker().onForgottenEverything(this.player, forgottenWaystones);
                } else {
                    for (var uuid : forgottenWaystones) {
                        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.invoker().onForgotten(this.player, uuid, storage.getPosition(uuid));
                    }
                }
            }
        }
    }

    //--

    public void discoverWaystones(Set<UUID> toLearn) {
        if (player.getWorld().isClient()) return;

        toLearn.forEach(uuid -> discoverWaystone(uuid, false));

        if (!toLearn.isEmpty()) syncDataChange(DISCOVERED_WAYSTONES_KEY);
    }

    public void discoverWaystone(UUID uuid) {
        discoverWaystone(uuid, true);
    }

    public void discoverWaystone(UUID uuid, boolean sync) {
        if (player.getWorld().isClient()) return;

        var storage = WaystoneDataStorage.getStorage(player);

        WaystoneEvents.ON_WAYSTONE_DISCOVERY.invoker().onDiscovery(this.player, uuid, storage.getPosition(uuid));
        addDiscoveredWaystone(uuid);
        if (sync) syncDataChange(DISCOVERED_WAYSTONES_KEY);
    }

    private void addDiscoveredWaystone(UUID uuid) {
        discoveredWaystones.add(uuid);
    }

    public boolean hasDiscoverdWaystone(UUID uuid) {
        return discoveredWaystones.contains(uuid);
    }

    //--

    public void forgetWaystones(Set<UUID> toForget) {
        forgetWaystones(toForget, true);
    }

    public void forgetWaystones(Set<UUID> toForget, boolean sync) {
        toForget.forEach(uuid -> this.forgetWaystone(uuid, false));

        if (!toForget.isEmpty() && sync) {
            syncDataChange(DISCOVERED_WAYSTONES_KEY);
        }
    }

    public void forgetAllWaystones() {
        if (discoveredWaystones.isEmpty()) return;

        var forgottenWaystones = new HashSet<>(discoveredWaystones);

        forgetWaystones(forgottenWaystones);

        WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.invoker().onForgottenEverything(this.player, forgottenWaystones);
    }

    public void forgetWaystone(UUID uuid) {
        forgetWaystone(uuid, true);
    }

    private void forgetWaystone(UUID uuid, boolean sync) {
        var data = WaystoneDataStorage.getStorage(player).getData(uuid);

        if (data != null) {
            if (data.global()) return;

            var server = this.player.getServer();

            if ((server != null && !server.isDedicated()) || this.player.getUuid().equals(data.owner())) {
                data.setOwner(null);
            }
        }

        discoveredWaystones.remove(uuid);

        if (sync) syncDataChange(DISCOVERED_WAYSTONES_KEY);
    }

    //--

    public Set<UUID> discoveredWaystones() {
        return Collections.unmodifiableSet(discoveredWaystones);
    }

    public List<Text> sortedPositionedDiscoveredWaystones() {
        var storage = WaystoneDataStorage.getStorage(player);

        var waystoneNames = new ArrayList<Text>();

        for (var uuid : discoveredWaystones) {
            if (storage.hasPosition(uuid)) {
                waystoneNames.add(storage.getData(uuid).name());
            }
        }

        waystoneNames.sort(Comparator.comparing(Text::getString, String::compareTo));

        return waystoneNames;
    }

    public List<UUID> sortedPositionedDiscoveredHashs() {
        var storage = WaystoneDataStorage.getStorage(player);

        var waystones = new ArrayList<UUID>();

        for (var uuid : discoveredWaystones) {
            if (storage.hasPosition(uuid)) {
                waystones.add(uuid);
            }
        }

        waystones.sort(Comparator.comparing(a -> storage.getData(a).nameAsString(), String::compareTo));

        return waystones;
    }

    public List<Text> sortedDiscoveredWaystones() {
        var storage = WaystoneDataStorage.getStorage(player);

        var waystoneNames = new ArrayList<Text>();

        for (var uuid : discoveredWaystones) {
            if (storage.hasData(uuid)) {
                waystoneNames.add(storage.getData(uuid).name());
            }
        }

        waystoneNames.sort(Comparator.comparing(Text::getString, String::compareTo));

        return waystoneNames;
    }

    public List<UUID> sortedDiscoveredHashs() {
        var storage = WaystoneDataStorage.getStorage(player);

        var waystones = new ArrayList<UUID>();

        for (var hash : discoveredWaystones) {
            if (storage.hasData(hash)) {
                waystones.add(hash);
            }
        }

        waystones.sort(Comparator.comparing(a -> storage.getData(a).nameAsString(), String::compareTo));

        return waystones;
    }

    public boolean viewDiscoveredWaystones() {
        return viewDiscoveredWaystones;
    }

    public void toggleViewDiscoveredWaystones() {
        viewDiscoveredWaystones = !viewDiscoveredWaystones;

        syncDataChange(VIEW_DISCOVERED_WAYSTONES_KEY);
    }

    public boolean viewGlobalWaystones() {
        return viewGlobalWaystones;
    }

    public void toggleViewGlobalWaystones() {
        viewGlobalWaystones = !viewGlobalWaystones;

        syncDataChange(VIEW_GLOBAL_WAYSTONES_KEY);
    }

    public boolean autofocusWaystoneFields() {
        return autofocusWaystoneFields;
    }

    public void toggleAutofocusWaystoneFields() {
        autofocusWaystoneFields = !autofocusWaystoneFields;

        syncDataChange(AUTOFOCUS_WAYSTONE_FIELDS_KEY);
    }

    public SearchType waystoneSearchType() {
        return waystoneSearchType;
    }

    public void waystoneSearchType(SearchType searchType) {
        this.waystoneSearchType = searchType;

        syncDataChange(WAYSTONE_SEARCH_TYPE_KEY);
    }

    public int teleportCooldown() {
        return teleportCooldown;
    }

    public void teleportCooldown(int teleportCooldown) {
        this.teleportCooldown = teleportCooldown;

        syncDataChange(TELEPORT_COOLDOWN_KEY);
    }

    //--

    public <T> void syncDataChange(WaystonePlayerDataKey<T> key) {
        if (this.player instanceof ServerPlayerEntity serverPlayerEntity) {
            WaystoneNetworkHandler.CHANNEL.serverHandle(serverPlayerEntity).send(new SyncWaystonePlayerDataChange(key, key.get(this)));
        } else {
            if (key.equals(TELEPORT_COOLDOWN_KEY)) return;

            syncDataToServer(key);
        }
    }

    @Environment(EnvType.CLIENT)
    private <T> void syncDataToServer(WaystonePlayerDataKey<T> key) {
        if (!(player instanceof AbstractClientPlayerEntity)) return;

        WaystoneNetworkHandler.CHANNEL.clientHandle().send(new SyncWaystonePlayerDataChange(key, key.get(this)));
    }

    public void syncDataChange() {
        if (this.player instanceof ServerPlayerEntity serverPlayerEntity) {
            WaystoneNetworkHandler.CHANNEL.serverHandle(serverPlayerEntity).send(new SyncWaystonePlayerData(this));
        }
    }

    private static <T, C extends Collection<T>> Endec<C> collectionOf(Endec<T> endec, Supplier<C> collectionCreator) {
        return endec.listOf().xmap(ts -> {
            var c = collectionCreator.get();
            c.addAll(ts);
            return c;
        }, ArrayList::new);
    }

    private static <S, T extends Collection<?>> StructField<S, T> optionalFieldOf(Endec<T> endec, String name, Function<S, T> getter, Supplier<@Nullable T> defaultValue) {
        return new StructField<>(name, endec.optionalOf().xmap(optional -> optional.orElseGet(defaultValue), objects -> {
            if (objects == null || objects.isEmpty()) return Optional.empty();

            return Optional.of(objects);
        }), getter, defaultValue);
    }

    public record OverrideData(Text text, int color) {

    }
}
