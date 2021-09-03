package wraith.waystones;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompatibilityLayer {

    private final WaystoneStorage STORAGE;
    private final ConcurrentHashMap<String,HashSet<String>> COMPATABILITY_MAP = new ConcurrentHashMap<>();
    private boolean COMPATABILITY_ENABLED = false;
    private final MinecraftServer SERVER;
    private static final String OLD_ID = Waystones.MOD_ID + "_waystones";

    public CompatibilityLayer(WaystoneStorage storage, MinecraftServer server) {
        SERVER = server;
        STORAGE = storage;
    }

    public boolean loadCompatibility() {
        PersistentState compatState = this.SERVER.getWorld(ServerWorld.OVERWORLD).getPersistentStateManager().get(() -> new PersistentState(OLD_ID) {
            @Override
            public void fromTag(NbtCompound tag) {
                CompatibilityLayer.this.fromTag(tag);
            }
            @Override
            public NbtCompound writeNbt(NbtCompound tag) {
                return CompatibilityLayer.this.toTag(tag);
            }
        }, OLD_ID);

        return compatState != null;
    }

    public void fromTag(NbtCompound tag) {
        if(tag.contains("Waystones")) {
            Waystones.LOGGER.info("[Fabric-Waystones] Found old save data. Converting to new save format.");
            COMPATABILITY_ENABLED = true;
            parseOldTag(tag);
        }
        else if(tag.contains("waystones_compat")){
            COMPATABILITY_ENABLED = true;
            parseCompatTag(tag);
        }
    }

    private void parseOldTag(NbtCompound tag) {
        NbtList compatTags = tag.getList("Waystones", 10);
        HashSet<WaystoneBlockEntity> entities = new HashSet<>();
        for(int i = 0; i < compatTags.size(); i++) {
            NbtCompound waystoneTag = compatTags.getCompound(i);

            String name = waystoneTag.getString("Name");
            ServerWorld world = getWorld(waystoneTag.getString("World"));
            if(world == null) {
                continue;
            }
            int[] coords = waystoneTag.getIntArray("Coordinates");
            WaystoneBlockEntity entity = WaystoneBlock.getEntity(world, new BlockPos(coords[0], coords[1], coords[2]));
            if(entity == null) {
                continue;
            }
            entity.setName(name);
            String hash = entity.getHash();
            entities.add(entity);

            NbtList players = waystoneTag.getList("DiscoveredBy", 10);
            for (int ii = 0; ii < players.size(); ii++) {
                NbtCompound player = players.getCompound(ii);
                String playerName = player.getString("PlayerName");
                if(playerName.equals("")) continue;
                if(!COMPATABILITY_MAP.containsKey(playerName)) {
                    COMPATABILITY_MAP.put(playerName, new HashSet<>());
                }
                COMPATABILITY_MAP.get(playerName).add(hash);
            }
        }
        STORAGE.addWaystones(entities);
    }

    private void parseCompatTag(NbtCompound tag) {
        NbtList compatTags = tag.getList("waystones_compat", 10);
        for(int i = 0; i < compatTags.size(); i++) {
            NbtCompound playerTag = compatTags.getCompound(i);
            if(playerTag.contains("username") && playerTag.contains("waystones")) {
                String user = playerTag.getString("username");
                if(user.equals("")) continue;
                NbtList knownWaystones = playerTag.getList("waystones", 10);
                HashSet<String> waystones = new HashSet<>();
                for (int j = 0; j < knownWaystones.size(); j++) {
                    String hash = knownWaystones.getString(j);
                    if(!hash.equals("")) waystones.add(hash);
                }
                COMPATABILITY_MAP.putIfAbsent(user, waystones);
            }
        }
    }

    public NbtCompound toTag(NbtCompound tag) {
        if(COMPATABILITY_ENABLED) return tag;
        if(tag == null) tag = new NbtCompound();

        NbtList compatWaystones = new NbtList();
        for (Map.Entry<String, HashSet<String>> player: COMPATABILITY_MAP.entrySet()) {
            NbtCompound playerTag = new NbtCompound();
            playerTag.putString("username", player.getKey());
            NbtList knownWaystones = new NbtList();
            for (String hash: player.getValue()) {
                NbtCompound waystone = new NbtCompound();
                waystone.putString("hash", hash);
                knownWaystones.add(waystone);
            }
            playerTag.put("waystones", knownWaystones);
            compatWaystones.add(playerTag);
        }
        tag.put("waystones_compat", compatWaystones);
        return tag;
    }

    private ServerWorld getWorld(String world) {
        if (SERVER == null) {
            return null;
        }
        for (ServerWorld worlds : SERVER.getWorlds()) {
            String id = worlds.getRegistryKey().getValue().toString();
            if (id.equals(world)) {
                return worlds;
            }
        }
        return null;
    }

    public void updatePlayerCompatibility(PlayerEntity player) {
        String username = player.getName().asString();
        if(COMPATABILITY_MAP.containsKey(player.getName().asString())) {
            HashSet<String> stonesToLearn = COMPATABILITY_MAP.get(username);
            COMPATABILITY_MAP.remove(username);
            ((PlayerEntityMixinAccess)player).discoverWaystones(stonesToLearn);
        }
    }
    
}
