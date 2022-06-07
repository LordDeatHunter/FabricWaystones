package wraith.fwaystones.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.access.WaystoneValue;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.integration.event.WaystoneEvents;
import wraith.fwaystones.mixin.MinecraftServerAccessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneStorage {

    private static final String ID = "fw_waystones";
    private final PersistentState state;
    private final ConcurrentHashMap<String, WaystoneValue> WAYSTONES = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    private final CompatibilityLayer compat;

    public WaystoneStorage(MinecraftServer server) {
        if (server == null) {
            this.server = null;
            this.state = null;
            this.compat = null;
            return;
        }
        CompatibilityLayer compatLoading = new CompatibilityLayer(this, server);
        this.server = server;

        // TODO: Remove more compat stuff...
        var savedPlayersPath = server.getSavePath(WorldSavePath.PLAYERDATA);
        if (Files.isDirectory(savedPlayersPath)) {
            try (var fileList = Files.list(savedPlayersPath)) {
                var iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    Path savedPlayerFile = iterator.next();
                    if (Files.isDirectory(savedPlayerFile) || !savedPlayerFile.toString().endsWith(".dat")) {
                        continue;
                    }
                    NbtCompound rawNbt = NbtIo.readCompressed(savedPlayerFile.toFile());
                    int dataVersion = rawNbt.contains("DataVersion", 3) ? rawNbt.getInt("DataVersion") : -1;
                    NbtHelper.update(Schemas.getFixer(), DataFixTypes.PLAYER, rawNbt, dataVersion);
                    if (rawNbt.contains("waystones")) {
                        var waystoneData = rawNbt.getCompound("waystones");
                        rawNbt.put(FabricWaystones.MOD_ID, waystoneData);
                        rawNbt.remove("waystones");
                        NbtIo.writeCompressed(rawNbt, savedPlayerFile.toFile());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // end me

        // TODO: Remove compat stuff
        var worldDirectory = ((MinecraftServerAccessor) server).getSession()
            .getWorldDirectory(server.getOverworld().getRegistryKey()).toFile();
        var file = new File(worldDirectory, "data/waystones:waystones.dat");
        if (file.exists()) {
            file.renameTo(new File(worldDirectory, "data/" + ID + ".dat"));
        }
        // end compat stuff

        var pState = new PersistentState() {
            @Override
            public NbtCompound writeNbt(NbtCompound tag) {
                return toTag(tag);
            }
        };
        state = this.server.getWorld(ServerWorld.OVERWORLD).getPersistentStateManager().getOrCreate(
            nbtCompound -> {
                fromTag(nbtCompound);
                return pState;
            },
            () -> pState, ID);

        if (!compatLoading.loadCompatibility()) {
            compatLoading = null;
        }
        compat = compatLoading;

        loadOrSaveWaystones(false);
    }

    public void fromTag(NbtCompound tag) {
//        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
//            return;
//        }
// TODO: Remove compat stuff
        String tagId;
        if (tag == null) {
            return;
        }
        if (tag.contains("waystones")) {
            tagId = "waystones";
        } else if (tag.contains("fwaystones")) {
            tagId = "fwaystones";
        } else {
            return;
        }
// end compat stuff
        WAYSTONES.clear();

        var globals = new HashSet<String>();
        for (var element : tag.getList("global_waystones", NbtElement.STRING_TYPE)) {
            globals.add(element.asString());
        }

        var waystones = tag.getList(tagId, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < waystones.size(); ++i) {
            NbtCompound waystoneTag = waystones.getCompound(i);
            if (!waystoneTag.contains("hash") || !waystoneTag.contains("name")
                || !waystoneTag.contains("dimension") || !waystoneTag.contains("position")) {
                continue;
            }
            String name = waystoneTag.getString("name");
            String hash = waystoneTag.getString("hash");
            String dimension = waystoneTag.getString("dimension");
            int[] coordinates = waystoneTag.getIntArray("position");
            int color = waystoneTag.contains("color", NbtType.INT) ? waystoneTag.getInt("color") : Utils.getRandomColor();
            BlockPos pos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
            WAYSTONES.put(hash, new Lazy(name, pos, hash, dimension, color, globals.contains(hash)));
        }
    }

    public NbtCompound toTag(NbtCompound tag) {
        if (tag == null) {
            tag = new NbtCompound();
        }
        NbtList waystones = new NbtList();
        for (Map.Entry<String, WaystoneValue> waystone : WAYSTONES.entrySet()) {
            String hash = waystone.getKey();
            WaystoneValue entity = waystone.getValue();
            NbtCompound waystoneTag = new NbtCompound();
            waystoneTag.putString("hash", hash);
            waystoneTag.putString("name", entity.getWaystoneName());
            waystoneTag.putInt("color", entity.getColor());
            BlockPos pos = entity.way_getPos();
            waystoneTag.putIntArray("position", Arrays.asList(pos.getX(), pos.getY(), pos.getZ()));
            waystoneTag.putString("dimension", entity.getWorldName());

            waystones.add(waystoneTag);
        }
        tag.put("fwaystones", waystones);
        NbtList globals = new NbtList();
        var globalWaystones = getGlobals();
        for (String globalWaystone : globalWaystones) {
            globals.add(NbtString.of(globalWaystone));
        }
        tag.put("global_waystones", globals);
        return tag;
    }

    public boolean hasWaystone(WaystoneBlockEntity waystone) {
        return WAYSTONES.containsValue(waystone);
    }

    public void tryAddWaystone(WaystoneBlockEntity waystone) {
        if (waystone == null || hasWaystone(waystone)) {
            return;
        }
        WAYSTONES.put(waystone.getHash(), waystone);
        loadOrSaveWaystones(true);
    }

    public void addWaystones(HashSet<WaystoneBlockEntity> waystones) {
        var added = false;
        for (WaystoneBlockEntity waystone : waystones) {
            if (waystone != null) {
                added = true;
                tryAddWaystone(waystone);
            }
        }
        if (added) {
            loadOrSaveWaystones(true);
        }
    }

    public void loadOrSaveWaystones(boolean save) {
        if (server == null) {
            return;
        }
        ServerWorld world = server.getWorld(ServerWorld.OVERWORLD);

        if (save) {
            state.markDirty();
            sendToAllPlayers();
        } else {
            try {
                NbtCompound compoundTag = world.getPersistentStateManager()
                    .readNbt(ID, SharedConstants.getGameVersion().getWorldVersion());
                state.writeNbt(compoundTag.getCompound("data"));
            } catch (IOException ignored) {
            }
        }
        world.getPersistentStateManager().save();
    }

    public void sendToAllPlayers() {
        if (server == null) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendToPlayer(player);
        }
    }

    public void sendToPlayer(ServerPlayerEntity player) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeNbt(toTag(new NbtCompound()));
        ServerPlayNetworking.send(player, WaystonePacketHandler.WAYSTONE_PACKET, data);
    }

    public void removeWaystone(String hash) {
        WaystoneEvents.REMOVE_WAYSTONE_EVENT.invoker().onRemove(hash);
        WAYSTONES.remove(hash);
        forgetForAllPlayers(hash);
        loadOrSaveWaystones(true);
    }

    public void removeWorldWaystones(String dimension) {
        if (server == null) {
            return;
        }
        WAYSTONES.forEach((hash, waystone) -> {
            if (waystone.getWorldName().equals(dimension)) {
                var entity = waystone.getEntity();
                if (entity != null) {
                    entity.setOwner(null);
                }
                removeWaystone(hash);
            }
        });
    }

    private void forgetForAllPlayers(String hash) {
        if (server == null) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ((PlayerEntityMixinAccess) player).forgetWaystone(hash);
        }
    }

    public void removeWaystone(WaystoneBlockEntity waystone) {
        removeWaystone(waystone.getHash());
    }

    public void renameWaystone(String hash, String name) {
        if (WAYSTONES.containsKey(hash)) {
            WaystoneValue waystone = WAYSTONES.get(hash);
            waystone.getEntity().setName(name);
            WaystoneEvents.RENAME_WAYSTONE_EVENT.invoker().onUpdate(hash);
            loadOrSaveWaystones(true);
        }
    }

    public void recolorWaystone(String hash, int color) {
        if (WAYSTONES.containsKey(hash)) {
            WAYSTONES.get(hash).setColor(color);
            loadOrSaveWaystones(true);
        }
    }

    @Nullable
    public WaystoneBlockEntity getWaystoneEntity(String hash) {
        WaystoneValue value = getWaystoneData(hash);
        return value != null ? value.getEntity() : null;
    }

    @Nullable
    public WaystoneValue getWaystoneData(String hash) {
        return WAYSTONES.getOrDefault(hash, null);
    }

    public boolean containsHash(String hash) {
        return WAYSTONES.containsKey(hash);
    }

    public List<String> getGlobals() {
        return WAYSTONES.entrySet().stream().filter(entry -> entry.getValue().isGlobal())
            .map(Map.Entry::getKey).toList();
    }

    public void toggleGlobal(String hash) {
        WaystoneBlockEntity waystone = getWaystoneEntity(hash);
        if (waystone == null) {
            return;
        }
        waystone.toggleGlobal();
        sendToAllPlayers();
    }

    public void setOwner(String hash, PlayerEntity owner) {
        if (WAYSTONES.containsKey(hash)) {
            WAYSTONES.get(hash).getEntity().setOwner(owner);
        }
    }

    public HashSet<String> getAllHashes() {
        return new HashSet<>(WAYSTONES.keySet());
    }

    public int getCount() {
        return WAYSTONES.size();
    }

    @Nullable
    public String getName(String hash) {
        WaystoneValue value = getWaystoneData(hash);
        return value != null ? value.getWaystoneName() : null;
    }

    public void sendCompatData(ServerPlayerEntity player) {
        if (this.compat != null) {
            this.compat.updatePlayerCompatibility(player);
        }
    }

    final class Lazy implements WaystoneValue {

        /**
         * unresolved name
         */
        final String name;
        final BlockPos pos;
        final String hash;
        final String dimension;
        final boolean isGlobal;
        int color;
        WaystoneBlockEntity entity;
        World world;

        Lazy(String name, BlockPos pos, String hash, String dimension, int color, boolean global) {
            this.name = name;
            this.pos = pos;
            this.hash = hash;
            this.dimension = dimension;
            this.color = color;
            this.isGlobal = global;
        }

        @Override
        public WaystoneBlockEntity getEntity() {
            if (server == null) {
                return null;
            }
            if (this.entity == null) {
                for (ServerWorld world : server.getWorlds()) {
                    if (Utils.getDimensionName(world).equals(dimension)) {
                        WaystoneBlockEntity entity = WaystoneBlock.getEntity(world, pos);
                        if (entity != null) {
                            tryAddWaystone(entity); // should allow this instance to be GCed
                            this.entity = entity;
                            this.world = world;
                        }
                        break;
                    }
                }
            }
            return this.entity;
        }

        @Override
        public String getWaystoneName() {
            return name;
        }

        @Override
        public BlockPos way_getPos() {
            return pos;
        }

        @Override
        public String getWorldName() {
            return this.dimension;
        }

        @Override
        public String getHash() {
            return this.hash;
        }

        @Override
        public int getColor() {
            return this.color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
        }

        @Override
        public boolean isGlobal() {
            return this.isGlobal;
        }
    }
}
