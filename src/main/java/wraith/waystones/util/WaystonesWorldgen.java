package wraith.waystones.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import wraith.waystones.Waystones;

import java.util.HashMap;
import java.util.HashSet;

public final class WaystonesWorldgen {

    private WaystonesWorldgen() {
    }

    public static final HashSet<Identifier> WAYSTONE_STRUCTURES = new HashSet<>();
    public static final HashMap<Identifier, Identifier> VANILLA_VILLAGES = new HashMap<>();

    static {
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:mossy_stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:red_desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:red_nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(new Identifier("waystones:village_waystone"));

        VANILLA_VILLAGES.put(new Identifier("village/plains/houses"), new Identifier("waystones:village_waystone"));
        VANILLA_VILLAGES.put(new Identifier("village/desert/houses"), new Identifier("waystones:desert_village_waystone"));
        VANILLA_VILLAGES.put(new Identifier("village/savanna/houses"), new Identifier("waystones:village_waystone"));
        VANILLA_VILLAGES.put(new Identifier("village/taiga/houses"), new Identifier("waystones:village_waystone"));
        VANILLA_VILLAGES.put(new Identifier("village/snowy/houses"), new Identifier("waystones:village_waystone"));
    }

    public static void registerVillage(MinecraftServer server, Identifier village, Identifier waystone) {
        Waystones.LOGGER.info("Adding waystone " + waystone.toString() + " to village " + village.toString());
        Utils.addToStructurePool(server, village, waystone, 5);
    }

    public static void registerVanillaVillageWorldgen(MinecraftServer server) {
        for (var entry : VANILLA_VILLAGES.entrySet()) {
            registerVillage(server, entry.getKey(), entry.getValue());
        }
    }

}