package wraith.fwaystones.util;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.access.WaystoneValue;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneStorage {

    public static final String ID = "fwaystones";
    private final SavedData state;
    public final ConcurrentHashMap<String, WaystoneValue> WAYSTONES = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    public WaystoneStorage(MinecraftServer server) {
        if (server == null) {
            this.server = null;
            this.state = null;
            return;
        }
        this.server = server;

        var pState = new SavedData() {
            @Override
            public CompoundTag save(CompoundTag tag) {
                return toTag(tag);
            }
        };
        state = this.server.getLevel(ServerLevel.OVERWORLD).getDataStorage().computeIfAbsent(
                nbtCompound -> {
                    fromTag(nbtCompound);
                    return pState;
                },
                () -> pState, ID);

        loadOrSaveWaystones(false);
    }

    public void fromTag(CompoundTag tag) {
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return;
        }
        WAYSTONES.clear();

        var globals = new HashSet<String>();
        for (var element : tag.getList("global_waystones", Tag.TAG_STRING)) {
            globals.add(element.getAsString());
        }

        var waystones = tag.getList(Waystones.MOD_ID, Tag.TAG_COMPOUND);

        for (int i = 0; i < waystones.size(); ++i) {
            CompoundTag waystoneTag = waystones.getCompound(i);
            if (!waystoneTag.contains("hash") || !waystoneTag.contains("name")
                    || !waystoneTag.contains("dimension") || !waystoneTag.contains("position")) {
                continue;
            }
            String name = waystoneTag.getString("name");
            String hash = waystoneTag.getString("hash");
            String dimension = waystoneTag.getString("dimension");
            int[] coordinates = waystoneTag.getIntArray("position");
            int color = waystoneTag.contains("color", Tag.TAG_INT) ? waystoneTag.getInt("color") : Utils.getRandomColor();
            BlockPos pos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
            WAYSTONES.put(hash, new Lazy(name, pos, hash, dimension, color, globals.contains(hash)));
        }
    }

    public CompoundTag toTag(CompoundTag tag) {
        if (tag == null) {
            tag = new CompoundTag();
        }
        ListTag waystones = new ListTag();
        for (Map.Entry<String, WaystoneValue> waystone : WAYSTONES.entrySet()) {
            String hash = waystone.getKey();
            WaystoneValue entity = waystone.getValue();
            CompoundTag waystoneTag = new CompoundTag();
            waystoneTag.putString("hash", hash);
            waystoneTag.putString("name", entity.getWaystoneName());
            waystoneTag.putInt("color", entity.getColor());
            BlockPos pos = entity.way_getPos();
            waystoneTag.putIntArray("position", Arrays.asList(pos.getX(), pos.getY(), pos.getZ()));
            waystoneTag.putString("dimension", entity.getWorldName());

            waystones.add(waystoneTag);
        }
        tag.put(Waystones.MOD_ID, waystones);
        ListTag globals = new ListTag();
        var globalWaystones = getGlobals();
        for (String globalWaystone : globalWaystones) {
            globals.add(StringTag.valueOf(globalWaystone));
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
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        if (save) {
            state.setDirty();
            sendToAllPlayers();
        } else {
            try {
                CompoundTag compoundTag = level.getDataStorage()
                        .readTagFromDisk(ID, SharedConstants.getCurrentVersion().getProtocolVersion());
                state.save(compoundTag.getCompound("data"));
            } catch (IOException ignored) {
            }
        }
        level.getDataStorage().save();
    }

    public void sendToAllPlayers() {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendToPlayer(player);
        }
    }

    public void sendToPlayer(ServerPlayer player) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeNbt(toTag(new CompoundTag()));
        NetworkManager.sendToPlayer(player, PacketHandler.WAYSTONE_PACKET, data);
    }

    public void removeWaystone(String hash) {
        // TODO: WaystoneEvents.REMOVE_WAYSTONE_EVENT.invoker().onRemove(hash);
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

    public void forgetForAllPlayers(String hash) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
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
            // TODO: WaystoneEvents.RENAME_WAYSTONE_EVENT.invoker().onUpdate(hash);
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

    public void setOwner(String hash, Player owner) {
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
        Level level;

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
                for (ServerLevel level : server.getAllLevels()) {
                    if (Utils.getDimensionName(level).equals(dimension)) {
                        WaystoneBlockEntity entity = WaystoneBlock.getEntity(level, pos);
                        if (entity != null) {
                            tryAddWaystone(entity); // should allow this instance to be GCed
                            this.entity = entity;
                            this.level = level;
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
