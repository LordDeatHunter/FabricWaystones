package wraith.fwaystones.registry;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.FWConfigModel;
import wraith.fwaystones.util.Utils;

import java.util.HashMap;
import java.util.HashSet;

public final class WaystonesWorldgen {

    public static final HashMap<Identifier, Identifier> VANILLA_VILLAGES = new HashMap<>();
    public static final HashSet<Identifier> WAYSTONE_STRUCTURES = new HashSet<>();

    static {
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("mossy_stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("red_desert_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("red_nether_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("stone_brick_village_waystone"));
        WAYSTONE_STRUCTURES.add(FabricWaystones.id("village_waystone"));

        FabricWaystones.CONFIG.add_waystone_structure_piece().forEach((identifier, originalStructure) -> VANILLA_VILLAGES.put(Identifier.of(identifier), FabricWaystones.id(originalStructure)));
    }

    private WaystonesWorldgen() {}

    public static void registerVillage(MinecraftServer server, Identifier village, Identifier waystone) {
        if (FabricWaystones.CONFIG.worldgen.generate_in_villages()) {
            if (FabricWaystones.CONFIG.generalLoggingLevel().equals(FWConfigModel.LoggingLevel.ALL)) {
                Utils.addToStructurePool(server, village, waystone, FabricWaystones.CONFIG.worldgen.village_waystone_weight());
            }

            FabricWaystones.LOGGER.info("Adding waystone {} to village {}", waystone.toString(), village.toString());
        }
    }

    public static void registerVanillaVillageWorldgen(MinecraftServer server) {
        for (var entry : VANILLA_VILLAGES.entrySet()) {
            registerVillage(server, entry.getKey(), entry.getValue());
        }
    }

}