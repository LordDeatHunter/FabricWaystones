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

        FabricWaystones.CONFIG.add_waystone_structure_piece().forEach((identifier, originalStructure) -> VANILLA_VILLAGES.put(new Identifier(identifier), Utils.ID(originalStructure)));
    }

    private WaystonesWorldgen() {}

    public static void registerVillage(MinecraftServer server, Identifier village, Identifier waystone) {
        if (FabricWaystones.CONFIG.worldgen.generate_in_villages()) {
            FabricWaystones.LOGGER.info("Adding waystone " + waystone.toString() + " to village " + village.toString());
            Utils.addToStructurePool(server, village, waystone, FabricWaystones.CONFIG.worldgen.village_waystone_weight());
        }
    }

    public static void registerVanillaVillageWorldgen(MinecraftServer server) {
        for (var entry : VANILLA_VILLAGES.entrySet()) {
            registerVillage(server, entry.getKey(), entry.getValue());
        }
    }

}