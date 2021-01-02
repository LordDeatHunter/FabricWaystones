package wraith.waystones;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import wraith.waystones.block.WaystoneBlockEntity;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneDatabase {

    private static MinecraftServer SERVER = null;
    public Map<String, Waystone> WAYSTONES = new ConcurrentHashMap<>();
    public static final String IDENTIFIER = Waystones.MOD_ID + "_waystones";
    PersistentState state;

    public ServerWorld getWorld(String world) {
        if (SERVER == null) {
            return null;
        }
        for (ServerWorld worlds : SERVER.getWorlds()) {
            String id = worlds.getRegistryKey().getValue().getNamespace() + ":" + worlds.getRegistryKey().getValue().getPath();
            if (id.equals(world)) {
                return worlds;
            }
        }
        return null;
    }

    public ServerWorld getWorld(Waystone waystone) {
        return getWorld(waystone.world);
    }

    public WaystoneDatabase(MinecraftServer server) {
        SERVER = server;
        if (SERVER == null) {
            return;
        }
        state = server.getWorld(ServerWorld.OVERWORLD).getPersistentStateManager().getOrCreate(() -> new PersistentState(IDENTIFIER) {
            @Override
            public void fromTag(CompoundTag tag) {
                ListTag list = tag.getList("Waystones", 10);
                for(int i = 0; i < list.size(); ++i) {
                    CompoundTag compound = list.getCompound(i);
                    ListTag playerList = compound.getList("DiscoveredBy", 10);
                    HashSet<String> players = new HashSet<>();
                    for (int player = 0; player < playerList.size(); ++player) {
                        players.add(playerList.getCompound(player).getString("PlayerName"));
                    }
                    String name = compound.getString("Name");
                    String world = compound.getString("World");
                    String facing = compound.getString("Facing");
                    int[] coords = compound.getIntArray("Coordinates");
                    Waystone waystone = new Waystone(name, new BlockPos(coords[0], coords[1], coords[2]), world, facing, players);
                    WAYSTONES.put(name, waystone);
                }
            }
            @Override
            public CompoundTag toTag(CompoundTag tag) {
                ListTag list = new ListTag();
                int i = 0;
                for(Waystone waystone : WAYSTONES.values()) {
                    CompoundTag compound = new CompoundTag();
                    BlockPos pos = waystone.pos;
                    int[] coords = {pos.getX(), pos.getY(), pos.getZ()};
                    ListTag players = new ListTag();
                    int j = 0;
                    for (String player : waystone.discoveredBy) {
                        CompoundTag playerName = new CompoundTag();
                        playerName.putString("PlayerName", player);
                        players.add(j, playerName);
                        ++j;
                    }
                    compound.putIntArray("Coordinates", coords);
                    compound.putString("Name", waystone.name);
                    compound.putString("World", waystone.world);
                    compound.putString("Facing",waystone.facing);
                    compound.put("DiscoveredBy", players);
                    list.add(i, compound);
                    ++i;
                }
                tag.put("Waystones", list);
                return tag;
            }
        }, Waystones.MOD_ID + "waystones");
    }

    public Waystone getWaystoneFromClick(PlayerEntity player, int id) {
        int i = 0;
        List<String> sortedWaystones = new ArrayList<>(WAYSTONES.keySet());
        Collections.sort(sortedWaystones);
        for (String waystoneName : sortedWaystones) {
            Waystone waystone = WAYSTONES.get(waystoneName);
            if (waystone.discoveredBy.contains(player.getName().asString()) || SERVER == null || (Config.getInstance().canGlobalDiscover() && waystone.discoveredBy.size() > 0)) {
                if (id == i) {
                    return waystone;
                }
                ++i;
            }
        }
        return null;
    }

    public void addWaystone(String id, WaystoneBlockEntity block) {
        WAYSTONES.put(id, new Waystone(id, block));
        loadOrSaveWaystones(true);
    }

    public void addWaystone(Waystone waystone) {
        WAYSTONES.put(waystone.name, waystone);
        loadOrSaveWaystones(true);
    }

    public Waystone getWaystone(String id) {
        if (WAYSTONES.containsKey(id)) {
            return WAYSTONES.get(id);
        }
        return null;
    }
    public Waystone getWaystone(BlockPos pos, String world) {
        for (Waystone waystone : WAYSTONES.values()) {
            if (waystone.pos.equals(pos) && world.equals(waystone.world)) {
                return waystone;
            }
        }
        return null;
    }

    public boolean containsWaystone(String id) {
        return WAYSTONES.containsKey(id);
    }

    public boolean containsWaystone(WaystoneBlockEntity block) {
        for (Waystone waystone : WAYSTONES.values()) {
            if (waystone.pos == block.getPos()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsWaystone(Waystone waystone) {
        return WAYSTONES.containsValue(waystone);
    }

    public void removeWaystone(String id) {
        if (WAYSTONES.containsKey(id)) {
            Waystone waystone = WAYSTONES.get(id);
            waystone.discoveredBy.clear();
            WAYSTONES.remove(id);
        }
        loadOrSaveWaystones(true);
    }

    public void removeWaystone(WaystoneBlockEntity block) {
        String removal = "";
        String worldName = block.getWorld().getRegistryKey().getValue().getNamespace() + ":" + block.getWorld().getRegistryKey().getValue().getPath();
        for (Entry<String, Waystone> set : WAYSTONES.entrySet()) {
            if (set.getValue().pos.equals(block.getPos()) && set.getValue().world.equals(worldName)) {
                removal = set.getKey();
                break;
            }
        }
        if (!"".equals(removal)) {
            Waystone waystone = WAYSTONES.get(removal);
            waystone.discoveredBy.clear();
            WAYSTONES.remove(removal);
        }
        loadOrSaveWaystones(true);
    }

    public int getPlayerDiscoveredCount(PlayerEntity player) {
        int count = 0;
        for (Waystone waystone : WAYSTONES.values()) {
            if (waystone.discoveredBy.contains(player.getName().asString()) || SERVER == null || (Config.getInstance().canGlobalDiscover() && waystone.discoveredBy.size() > 0)) {
                ++count;
            }
        }
        return count;
    }

    public void discoverWaystone(PlayerEntity player, String id) {
        String name = player.getName().asString();
        WAYSTONES.get(id).discoveredBy.add(name);
        loadOrSaveWaystones(true);
    }

    public void forgetWaystone(PlayerEntity player, String id) {
        String name = player.getName().asString();
        if (WAYSTONES.containsKey(id)) {
            WAYSTONES.get(id).discoveredBy.remove(name);
        }
        loadOrSaveWaystones(true);
    }

    public void renameWaystone(String oldName, String newName) {
        WAYSTONES.put(newName, WAYSTONES.get(oldName).rename(newName));
        WAYSTONES.remove(oldName);
        loadOrSaveWaystones(true);
    }

    public List<String> getDiscoveredWaystoneNames(PlayerEntity player) {
        ArrayList<String> discovered = new ArrayList<>();
        String name = player.getName().asString();
        for (Waystone waystone : WAYSTONES.values()) {
            if (waystone.discoveredBy.contains(name) || SERVER == null || (Config.getInstance().canGlobalDiscover() && waystone.discoveredBy.size() > 0)) {
                discovered.add(waystone.name);
            }
        }
        Collections.sort(discovered);
        return discovered;
    }

    public List<Waystone> getDiscoveredWaystones(PlayerEntity player) {
        ArrayList<Waystone> discovered = new ArrayList<>();
        String name = player.getName().asString();
        List<String> sortedWaystones = new ArrayList<>(WAYSTONES.keySet());
        Collections.sort(sortedWaystones);
        for (String waystoneName : sortedWaystones) {
            Waystone waystone = WAYSTONES.get(waystoneName);
            if (waystone.discoveredBy.contains(name) || SERVER == null || (Config.getInstance().canGlobalDiscover() && waystone.discoveredBy.size() > 0)) {
                discovered.add(waystone);
            }
        }
        return discovered;
    }

    public boolean playerHasDiscovered(PlayerEntity player, String id) {
        return WAYSTONES.containsKey(id) && (WAYSTONES.get(id).discoveredBy.contains(player.getName().asString()) || SERVER == null);
    }

    public BlockPos getBlockPosition(String id) {
        if (WAYSTONES.containsKey(id)) {
            return WAYSTONES.get(id).pos;
        }
        return BlockPos.ORIGIN;
    }

    public Waystone getFromIntId(PlayerEntity player, int id) {
        int i = 0;
        for (Waystone waystone : WAYSTONES.values()) {
            for (String players : waystone.discoveredBy) {
                if (player.getName().asString().equals(players)) {
                    if (i == id) {
                        return waystone;
                    }
                    ++i;
                }
            }
        }
        return null;
    }

    public void loadOrSaveWaystones(boolean save) {
        if (SERVER == null) {
            return;
        }
        ServerWorld world = SERVER.getWorld(ServerWorld.OVERWORLD);

        if (save) {
            state.markDirty();
            sendToAllPlayers();
        }
        else {
            try {
                CompoundTag compoundTag = world.getPersistentStateManager().readTag(IDENTIFIER, SharedConstants.getGameVersion().getWorldVersion());
                state.fromTag(compoundTag.getCompound("data"));
            } catch (IOException ignored) {
            }
        }
        world.getPersistentStateManager().save();
    }

    public void sendToAllPlayers() {
        if (SERVER == null || !SERVER.isDedicated()) {
            return;
        }
        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            sendToPlayer(player);
        }
    }

    public void sendToPlayer(ServerPlayerEntity player) {
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        List<Waystone> waystones = getDiscoveredWaystones(player);
        ListTag list = new ListTag();
        int i = 0;
        for (Waystone waystone : waystones) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", waystone.name);
            tag.putString("World", waystone.world);
            tag.putString("Facing", waystone.facing);
            tag.putIntArray("Coordinates", new int[]{waystone.pos.getX(), waystone.pos.getY(), waystone.pos.getZ()});
            list.add(i, tag);
            ++i;
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Waystones", list);
        data.writeCompoundTag(tag);
        ServerPlayNetworking.send(player, Utils.ID("waystone_packet"), data);
    }
}
