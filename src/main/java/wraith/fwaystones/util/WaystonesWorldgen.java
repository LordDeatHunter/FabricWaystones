package wraith.fwaystones.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

import java.util.HashMap;
import java.util.HashSet;

public final class WaystonesWorldgen {

    public static final HashMap<Identifier, Identifier> VANILLA_VILLAGES = new HashMap<>();
    public static final HashSet<Identifier> WAYSTONE_STRUCTURES = new HashSet<>();

    static {
        WAYSTONE_STRUCTURES.add(Utils.ID("desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("mossy_stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("red_desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("red_nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(Utils.ID("village_waystone"));

        var structureList = Config.getInstance().getWorldgenStructures();
        structureList.getKeys().forEach(originalStructure -> VANILLA_VILLAGES.put(new Identifier(originalStructure), Utils.ID(structureList.getString(originalStructure))));
    }

    private WaystonesWorldgen() {}

    public static void registerVillage(MinecraftServer server, Identifier village, Identifier waystone) {
        var config = Config.getInstance();
        if (config.generateInVillages()) {
            FabricWaystones.LOGGER.info("Adding waystone " + waystone.toString() + " to village " + village.toString());
            Utils.addToStructurePool(server, village, waystone, config.getVillageStructureWeight());
        }
    }

    public static void registerVanillaVillageWorldgen(MinecraftServer server) {
        for (var entry : VANILLA_VILLAGES.entrySet()) {
            registerVillage(server, entry.getKey(), entry.getValue());
        }
    }

}