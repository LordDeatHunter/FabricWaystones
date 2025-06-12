package wraith.fwaystones.api;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.item.components.WaystoneDataHolder;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.s2c.SyncWaystoneDataChange;
import wraith.fwaystones.networking.packets.s2c.SyncWaystoneDataChanges;
import wraith.fwaystones.networking.packets.s2c.SyncWaystonePositionChange;
import wraith.fwaystones.networking.packets.s2c.SyncWaystonePositionChanges;
import wraith.fwaystones.client.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.api.core.WaystonePosition;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneDataStorage {

    private static final Endec<Map<String, Map<BlockPos, UUID>>> MAP_ENDEC =
            Endec.map(
                    pos -> String.valueOf(pos.asLong()),
                    s -> BlockPos.fromLong(Long.parseLong(s)),
                    BuiltInEndecs.UUID
            ).mapOf();

    public static final Endec<WaystoneDataStorage> ENDEC = StructEndecBuilder.of(
            MAP_ENDEC.fieldOf("positions", WaystoneDataStorage::exportPositions),
            WaystoneData.ENDEC.setOf().fieldOf("waystones", WaystoneDataStorage::exportData),
            WaystoneDataStorage::new
    );

    private static final AttachmentType<WaystoneDataStorage> ATTACHMENT_TYPE = AttachmentRegistry.<WaystoneDataStorage>builder()
            .persistent(CodecUtils.toCodec(ENDEC))
            .initializer(() -> new WaystoneDataStorage(Map.of(), Set.of()))
            .buildAndRegister(FabricWaystones.id("waystone_data_storage"));

    private static MinecraftServer SERVER;

    public static MinecraftServer getServer() {
        return SERVER;
    }

    public final Map<WaystonePosition, UUID> positionToUUID = new ConcurrentHashMap<>();
    public final Map<UUID, WaystonePosition> uuidToPosition = new ConcurrentHashMap<>();

    public final Map<UUID, WaystoneData> uuidToData = new ConcurrentHashMap<>();

    public final Map<WaystonePosition, WeakReference<WaystoneBlockEntity>> waystoneLookupCache = new ConcurrentHashMap<>();

    WaystoneDataStorage(Map<String, Map<BlockPos, UUID>> waystonePositions, Set<WaystoneData> waystones) {
        for (var data : waystones) {
            uuidToData.put(data.uuid(), data);
        }

        for (var worldEntry : waystonePositions.entrySet()) {
            var worldName = worldEntry.getKey();

            worldEntry.getValue().forEach((blockPos, uuid) -> {
                var pos = new WaystonePosition(worldName, blockPos);

                if (uuidToData.containsKey(uuid)) {
                    positionToUUID.put(pos, uuid);
                    uuidToPosition.put(uuid, pos);
                }
            });
        }
    }

    public Map<String, Map<BlockPos, UUID>> exportPositions() {
        var map = new HashMap<String, Map<BlockPos, UUID>>();

        for (var entry : uuidToPosition.entrySet()) {
            var uuid = entry.getKey();
            var position = entry.getValue();

            var worldMap = map.computeIfAbsent(position.worldName(), worldName -> new HashMap<>());

            worldMap.put(position.blockPos(), uuid);
        }

        return map;
    }

    public Set<WaystoneData> exportData() {
        return Set.copyOf(uuidToData.values());
    }

    @Deprecated
    @ApiStatus.Internal
    @Nullable
    public static WaystoneDataStorage getStorageUnsafe() {
        try {
            if (SERVER == null) {
                return getStorageUnsafeFromClient();
            } else {
                return getStorage(SERVER);
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Environment(EnvType.CLIENT)
    private static WaystoneDataStorage getStorageUnsafeFromClient() {
        var scoreBoard = MinecraftClient.getInstance().world.getScoreboard();

        return getStorage(scoreBoard);
    }

    public static WaystoneDataStorage getStorage(World world) {
        return getStorage(world.getScoreboard());
    }

    public static WaystoneDataStorage getStorage(PlayerEntity player) {
        return getStorage(player.getScoreboard());
    }

    public static WaystoneDataStorage getStorage(MinecraftServer server) {
        return getStorage(server.getScoreboard());
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public static WaystoneDataStorage getStorage(MinecraftClient client) {
        var world = client.world;

        if (world == null) return null;

        return getStorage(world);
    }

    public static WaystoneDataStorage getStorage(Scoreboard scoreboard) {
        return scoreboard.getAttachedOrCreate(ATTACHMENT_TYPE);
    }

    @Environment(EnvType.CLIENT)
    public static void setClientStorage(WaystoneDataStorage storage, PlayerEntity player) {
        if (!player.getWorld().isClient) return;

        player.getScoreboard().setAttached(ATTACHMENT_TYPE, storage);

        if (player.currentScreenHandler instanceof UniversalWaystoneScreenHandler) {
            ((UniversalWaystoneScreenHandler) player.currentScreenHandler).updateWaystones(player);
        }
    }

    private boolean isClient = true;

    //--

    public static final String ID = "fw_waystones";
    private final PersistentState.Type<PersistentState> type = new PersistentState.Type<>(this::createState, this::stateFromNbt, DataFixTypes.LEVEL);

    private PersistentState createState() {
        return new PersistentState() {
            @Override
            public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
                if (tag == null) tag = new NbtCompound();

                return tag;
            }
        };
    }

    private PersistentState stateFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return this.createState();
    }

    //--

    public void setupServerStorage(MinecraftServer server) {
        if (SERVER != null) return;

        SERVER = server;

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof WaystoneBlockEntity waystoneBe){
                waystoneLookupCache.remove(waystoneBe.position());
            }
        });

        this.isClient = false;

        setupOldState(server);
    }

    public void setupOldState(MinecraftServer server) {
        var stateManager = server.getWorld(ServerWorld.OVERWORLD).getPersistentStateManager();

        stateManager.getOrCreate(type, ID);

        try {
            var oldNbtData = stateManager.readNbt(ID, DataFixTypes.LEVEL, SharedConstants.getGameVersion().getProtocolVersion());

            fromTag(oldNbtData);
        } catch (IOException ignored) {}
    }

    public void fromTag(NbtCompound tag) {
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) return;

        var globals = new HashSet<String>();
        for (var element : tag.getList("global_waystones", NbtElement.STRING_TYPE)) {
            globals.add(element.asString());
        }

        var waystones = tag.getList(FabricWaystones.MOD_ID, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < waystones.size(); ++i) {
            NbtCompound waystoneTag = waystones.getCompound(i);
            if (!waystoneTag.contains("hash") || !waystoneTag.contains("name") || !waystoneTag.contains("dimension") || !waystoneTag.contains("position")) {
                continue;
            }
            String name = waystoneTag.getString("name");
            String dimension = waystoneTag.getString("dimension");
            String nbtHash = waystoneTag.getString("hash");

            int[] coordinates = waystoneTag.getIntArray("position");
            int color = waystoneTag.contains("color", NbtElement.INT_TYPE) ? waystoneTag.getInt("color") : Utils.getRandomColor();
            BlockPos pos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
            String hash = WaystonePosition.createHashString(dimension, pos);

            // Migrate global hashes from old hash method to new.
            if (!hash.equals(nbtHash) && globals.contains(nbtHash)) {
                globals.remove(nbtHash);
                globals.add(hash);
            }

            var position = new WaystonePosition(dimension, pos);
            var uuid = getUniqueUUID();

            var data = new WaystoneData(uuid, name, color, globals.contains(hash));

            positionToUUID.put(position, uuid);
            uuidToPosition.put(uuid, position);

            uuidToData.put(uuid, data);
        }
    }

    private UUID getUniqueUUID() {
        var uuid = WaystoneData.EMPTY_UUID;

        while(Objects.equals(uuid, WaystoneData.EMPTY_UUID) || hasData(uuid)) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public boolean isSetup() {
        return SERVER != null;
    }

    public void reset() {
        SERVER = null;

        this.positionToUUID.clear();
        this.uuidToPosition.clear();

        this.uuidToData.clear();

        this.waystoneLookupCache.clear();
    }

    public WaystonePosition getHashFromUnsafeHash(WaystonePosition position) {
        if (!position.isUnsafe()) return position;

        var data = getData(position);

        return getPosition(data);
    }

    //--

    @Nullable
    public WaystoneData getData(WaystonePosition position) {
        if (position.equals(WaystonePosition.EMPTY)) return null;

        var uuid = positionToUUID.get(position);

        if (uuid == null) return null;

        return getData(uuid);
    }

    public WaystoneData getData(UUID uuid) {
        return uuidToData.get(uuid);
    }

    public UUID getUUID(WaystonePosition position) {
        return positionToUUID.get(position);
    }

    public boolean hasData(WaystonePosition position) {
        if (position.equals(WaystonePosition.EMPTY)) return false;

        return positionToUUID.containsKey(position);
    }

    public boolean hasData(UUID uuid) {
        return uuidToData.containsKey(uuid);
    }

    public boolean hasPosition(UUID uuid) {
        return uuidToPosition.containsKey(uuid);
    }

    @Nullable
    public WaystonePosition getPosition(WaystoneData data) {
        return getPosition(data.uuid());
    }

    @Nullable
    public WaystonePosition getPosition(UUID uuid) {
        return uuidToPosition.get(uuid);
    }

    public Set<WaystonePosition> getAllPositions() {
        return Collections.unmodifiableSet(positionToUUID.keySet());
    }

    public Set<UUID> getAllIds() {
        return Collections.unmodifiableSet(uuidToData.keySet());
    }

    public WaystoneData createGetOrImportData(WaystoneBlockEntity blockEntity, int color) {
        if (isClient) return null;

        var pos = blockEntity.position();
        var holder = blockEntity.dataHolder;

        if (holder == null) {
            if (hasData(pos)) return getData(pos);
            var customName = blockEntity.getCustomName();
            //TODO: look into this, it probably shouldn't be "" if the name is null
            return createData(pos, customName != null ? customName.getString() : "", color);
        }

        var data = holder.data();

        if (hasData(data.uuid()) && hasPosition(data.uuid())) {
            var uuid = getUniqueUUID();

            data = data.cloneWithUUID(uuid);
        }

        addData(data);

        blockEntity.dataHolder = null;

        positionDataInWorld(pos, data);

        return data;
    }

    public WaystoneData createData(WaystonePosition position, String name, int color) {
        var data = new WaystoneData(name, color);

        addData(data);

        positionDataInWorld(position, data);

        return data;
    }

    @Nullable
    public WaystoneDataHolder removePositionAndExport(WaystoneBlockEntity blockEntity) {
        var uuid = blockEntity.getUUID();
        var data = getData(uuid);

        removePosition(uuid);

        return new WaystoneDataHolder(data);
    }

    public WaystoneDataHolder removePositionAndData(WaystoneBlockEntity blockEntity) {
        var uuid = blockEntity.getUUID();

        removePosition(uuid);

        var data = removeData(uuid, true);

        return new WaystoneDataHolder(data);
    }

    public void removePosition(UUID uuid) {
        removePosition(uuid, true);
    }

    private void removePosition(UUID uuid, boolean sync) {
        var position = uuidToPosition.remove(uuid);

        if (position != null) {
            positionToUUID.remove(position);

            if (sync) syncPositionChange(uuid, position, true);
        }
    }

    public void removeAllFromWorld(String worldName) {
        if (this.isClient) return;

        var keys = Set.copyOf(this.positionToUUID.keySet());

        var uuids = new HashSet<UUID>();

        for (var position : keys) {
            if (position.worldName().equals(worldName)) {
                var uuid = getUUID(position);

                if (uuid != null) {
                    removePosition(uuid, false);

                    uuids.add(uuid);
                }
            }
        }

        var positionChanges = new SyncWaystonePositionChanges(uuids.stream().map(uuid -> new SyncWaystonePositionChange(uuid,null)).toList());
        var dataChanges = new SyncWaystoneDataChanges(uuids.stream().map(uuid -> new SyncWaystoneDataChange(uuid, null, DataChangeType.REMOVAL)).toList());

        WaystoneNetworkHandler.CHANNEL.serverHandle(SERVER).send(positionChanges, dataChanges);
    }

    private void positionDataInWorld(WaystonePosition position, WaystoneData data) {
        if (position.equals(WaystonePosition.EMPTY)) {
            FabricWaystones.LOGGER.warn("Unable to create WaystoneData as it was found to be EMPTY Hash!");
            return;
        }

        if (position.isUnsafe()) {
            throw new IllegalStateException("Unable to set data with a unsafe WaystoneHash!");
        }

        var uuid = data.uuid();

        positionToUUID.put(position, uuid);
        uuidToPosition.put(uuid, position);

        syncPositionChange(uuid, position, false);
    }

    public WaystoneData removeData(UUID uuid, boolean sync) {
        var data = uuidToData.remove(uuid);

        if (sync) syncDataChange(uuid, DataChangeType.REMOVAL);

        return data;
    }

    private void addData(WaystoneData data) {
        var uuid = data.uuid();

        uuidToData.put(uuid, data);

        syncDataChange(uuid, DataChangeType.CREATION);
    }

    @Nullable
    public WaystoneBlockEntity getEntity(WaystoneData data) {
        return getEntity(data.uuid());
    }

    @Nullable
    public WaystoneBlockEntity getEntity(UUID uuid) {
        var pos = getPosition(uuid);

        if (pos == null) return null;

        return getEntity(pos);
    }

    @Nullable
    public WaystoneBlockEntity getEntity(WaystonePosition hash) {
        if (isClient) return null;

        var ref = waystoneLookupCache.get(hash);

        if (ref != null) return ref.get();

        WaystoneBlockEntity be = null;

        for (ServerWorld world : SERVER.getWorlds()) {
            if (Utils.getDimensionName(world).equals(hash.worldName())) {
                be = WaystoneBlock.getEntity(world, hash.blockPos());

                waystoneLookupCache.put(hash, new WeakReference<>(be));

                break;
            }
        }

        return be;
    }

    public SequencedCollection<UUID> getGlobals() {
        return this.uuidToData.entrySet().stream().filter(entry -> entry.getValue().global())
                .map(Map.Entry::getKey).toList();
    }


    public boolean isGlobal(WaystonePosition hash) {
        var uuid = getUUID(hash);

        return uuid != null && isGlobal(uuid);
    }

    public boolean isGlobal(UUID uuid) {
        var data = getData(uuid);

        return data != null && data.global();
    }

    //--

    public void syncDataChange(UUID uuid, DataChangeType type) {
        if (isClient) return;

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.invoker().onChange(uuid, type);

        WaystoneNetworkHandler.CHANNEL.serverHandle(SERVER).send(new SyncWaystoneDataChange(uuid, getData(uuid), type));
    }

    public void syncPositionChange(UUID uuid, WaystonePosition position, boolean wasRemoved) {
        if (isClient) return;

        WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.invoker().onChange(uuid, position, wasRemoved);

        WaystoneNetworkHandler.CHANNEL.serverHandle(SERVER).send(new SyncWaystonePositionChange(uuid, wasRemoved ? null : position));
    }

    public void onSyncData(UUID uuid, @Nullable WaystoneData data, DataChangeType type) {
        if (data == null) {
            this.uuidToData.remove(uuid);
        } else {
            this.uuidToData.put(uuid, data);
        }

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.invoker().onChange(uuid, type);
    }

    public void onSyncPosition(UUID uuid, @Nullable WaystonePosition position) {
        if (position == null) {
            var pos = this.uuidToPosition.remove(uuid);

            if (pos != null) this.positionToUUID.remove(pos);

            WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.invoker().onChange(uuid, pos, true);
        } else {
            this.uuidToPosition.put(uuid, position);

            var prevUUID = this.positionToUUID.put(position, uuid);

            if (prevUUID != null) {
                WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.invoker().onChange(prevUUID, position, true);
            }

            WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.invoker().onChange(uuid, position, false);
        }
    }

    public void renameWaystone(UUID uuid, String name) {
        var data = getData(uuid);

        if (data == null) return;

        data.setName(name);
        syncDataChange(uuid, DataChangeType.NAME);
    }

    public void recolorWaystone(UUID uuid, int color) {
        var data = getData(uuid);

        if (data == null) return;

        data.setColor(color);
        syncDataChange(uuid, DataChangeType.COLOR);
    }

    public void toggleGlobal(UUID uuid) {
        var data = getData(uuid);

        if (data == null) return;

        data.setGlobal(!data.global());
        syncDataChange(uuid, DataChangeType.GLOBAL);
    }

    public boolean setOwner(UUID uuid, @Nullable PlayerEntity owner) {
        var data = getData(uuid);

        if (data == null) return false;

        var prevOwner = data.owner();

        data.setOwner(owner);

        var changeOccured = !Objects.equals(prevOwner, owner == null ? null : owner.getUuid());

        if (changeOccured) syncDataChange(uuid, DataChangeType.OWNER);

        return changeOccured;
    }
}
