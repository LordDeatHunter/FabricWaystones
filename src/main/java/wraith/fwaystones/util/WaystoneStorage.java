package wraith.fwaystones.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.access.WaystoneValue;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.integration.event.WaystoneEvents;
import wraith.fwaystones.packets.client.WaystonePacket;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneStorage {

    public static final String ID = "fw_waystones";
    public final ConcurrentHashMap<String, WaystoneValue> WAYSTONES = new ConcurrentHashMap<>();
    private final SavedData state;
    private final MinecraftServer server;
    private final SavedDataType<State> type = new SavedDataType<>(
        ID,
        State::new,
        CompoundTag.CODEC.xmap(
            nbt -> {
                fromTag(nbt);
                return new State();
            },
            state -> toTag(new CompoundTag())
        ),
        DataFixTypes.LEVEL
    );

    public WaystoneStorage(MinecraftServer server) {
        if (server == null) {
            this.server = null;
            this.state = null;
            return;
        }
        this.server = server;

        state = this.server.getLevel(ServerLevel.OVERWORLD).getDataStorage().computeIfAbsent(type);

        loadWaystones();
    }

    private final class State extends SavedData {
    }

    public void fromTag(CompoundTag tag) {
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return;
        }
        WAYSTONES.clear();

        var globals = new HashSet<String>();
        for (var element : tag.getListOrEmpty("global_waystones")) {
            element.asString().ifPresent(globals::add);
        }

        var waystones = tag.getListOrEmpty(FabricWaystones.MOD_ID);

        for (int i = 0; i < waystones.size(); ++i) {
            CompoundTag waystoneTag = waystones.getCompoundOrEmpty(i);
            if (!waystoneTag.contains("hash") || !waystoneTag.contains("name")
                || !waystoneTag.contains("dimension") || !waystoneTag.contains("position")) {
                continue;
            }
            String name = waystoneTag.getStringOr("name", "");
            String dimension = waystoneTag.getStringOr("dimension", "");
            String nbtHash = waystoneTag.getStringOr("hash", "");

            int[] coordinates = waystoneTag.getIntArray("position").orElse(null);
            if (coordinates == null || coordinates.length < 3) {
                continue;
            }
            int color = waystoneTag.getInt("color").orElseGet(Utils::getRandomColor);
            BlockPos pos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
            String hash = WaystoneBlockEntity.createHashString(dimension, pos);

            // Migrate global hashes from old hash method to new.
            if (!hash.equals(nbtHash) && globals.contains(nbtHash)) {
                globals.remove(nbtHash);
                globals.add(hash);
            }

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
            if (!entity.getHash().equals(hash)) {
                continue;
            }
            CompoundTag waystoneTag = new CompoundTag();
            waystoneTag.putString("hash", hash);
            waystoneTag.putString("name", entity.getWaystoneName());
            waystoneTag.putInt("color", entity.getColor());
            BlockPos pos = entity.way_getPos();
            waystoneTag.putIntArray("position", new int[]{pos.getX(), pos.getY(), pos.getZ()});
            waystoneTag.putString("dimension", entity.getWorldName());

            waystones.add(waystoneTag);
        }
        tag.put(FabricWaystones.MOD_ID, waystones);
        ListTag globals = new ListTag();
        var globalWaystones = getGlobals();
        for (String globalWaystone : globalWaystones) {
            globals.add(StringTag.valueOf(globalWaystone));
        }
        tag.put("global_waystones", globals);
        return tag;
    }

    public void tryAddWaystone(WaystoneBlockEntity waystone) {
        if (waystone == null) {
            return;
        }
        if (WAYSTONES.containsValue(waystone)) {
            return;
        }
        boolean alreadyExists = WAYSTONES.containsKey(waystone.getHash());
        WAYSTONES.put(waystone.getHash(), waystone);
        saveWaystones(!alreadyExists);
    }

    public void tryAddWaystoneFromItemstack(WaystoneBlockEntity waystone, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            tryAddWaystone(waystone);
            return;
        }
        CustomData tag = itemStack.get(DataComponents.CUSTOM_DATA);
        if (tag != null) {
            CompoundTag nbt = tag.copyTag();
            if (nbt != null) {
                nbt.getString("waystone_name").ifPresent(waystone::setName);
                nbt.getBoolean("waystone_is_global").ifPresent(waystone::setGlobal);
                nbt.getInt("waystone_color").ifPresent(waystone::setColor);
                if (nbt.contains("Items")) {
                    NonNullList<ItemStack> inventory = waystone.getInventory();
                    ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, waystone.getLevel().registryAccess(), nbt);
                    ContainerHelper.loadAllItems(input, inventory);
                    waystone.setInventory(inventory);
                }
            }
        }

        tryAddWaystone(waystone);
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
            saveWaystones(true);
        }
    }

    public void saveWaystones(boolean sync) {
        if (server == null) {
            return;
        }
        ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);

        state.setDirty();
        if (sync) {
            sendToAllPlayers();
        }
        world.getDataStorage().saveAndJoin();
    }

    public void loadWaystones() {
        if (server == null) {
            return;
        }
        // Loading happens through the codec in getOrCreate; just persist the initial state.
        server.getLevel(ServerLevel.OVERWORLD).getDataStorage().saveAndJoin();
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
        WaystonePacket packet = new WaystonePacket(toTag(null));
        ServerPlayNetworking.send(player, packet);
    }

    public void removeWaystone(String hash) {
        WaystoneEvents.REMOVE_WAYSTONE_EVENT.invoker().onRemove(hash);
        WAYSTONES.remove(hash);
        saveWaystones(false);
        forgetForAllPlayers(hash);
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
            ((PlayerEntityMixinAccess) player).fabricWaystones$forgetWaystone(hash);
        }
    }

    public void removeWaystone(WaystoneBlockEntity waystone) {
        removeWaystone(waystone.getHash());
    }

    public boolean removeIfInvalid(String hash) {
        if (WAYSTONES.containsKey(hash) && getWaystoneEntity(hash) == null) {
            removeWaystone(hash);
            return true;
        }
        return false;
    }

    public void renameWaystone(String hash, String name) {
        if (WAYSTONES.containsKey(hash)) {
            WaystoneValue waystone = WAYSTONES.get(hash);
            waystone.getEntity().setName(name);
            WaystoneEvents.RENAME_WAYSTONE_EVENT.invoker().onUpdate(hash);
            saveWaystones(true);
        }
    }

    public void recolorWaystone(String hash, int color) {
        if (WAYSTONES.containsKey(hash)) {
            WAYSTONES.get(hash).setColor(color);
            saveWaystones(true);
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

    public boolean isGlobal(String hash) {
        return WAYSTONES.containsKey(hash) && WAYSTONES.get(hash).isGlobal();
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
        Level world;

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
                for (ServerLevel world : server.getAllLevels()) {
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
