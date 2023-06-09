package wraith.fwaystones.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import wraith.fwaystones.Waystones;

import java.util.HashMap;
import java.util.HashSet;

public final class Worldgen {
	public static final HashMap<ResourceLocation, ResourceLocation> VANILLA_VILLAGES = new HashMap<>();
	public static final HashSet<ResourceLocation> WAYSTONE_STRUCTURES = new HashSet<>();

	static {
		WAYSTONE_STRUCTURES.add(Utils.ID("desert_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("mossy_stone_brick_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("nether_brick_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("red_desert_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("red_nether_brick_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("stone_brick_village_waystone"));
		WAYSTONE_STRUCTURES.add(Utils.ID("village_waystone"));

		Waystones.CONFIG.add_waystone_structure_piece.forEach((identifier, originalStructure) -> VANILLA_VILLAGES.put(new ResourceLocation(identifier), Utils.ID(originalStructure)));
	}

	public static void registerVillage(MinecraftServer server, ResourceLocation village, ResourceLocation waystone) {
		if (Waystones.CONFIG.worldgen.generate_in_villages) {
			Waystones.LOGGER.info("Adding waystone " + waystone.toString() + " to village " + village.toString());
			Utils.addToStructurePool(server, village, waystone, Waystones.CONFIG.worldgen.village_waystone_weight);
		}
	}

	public static void registerVanillaVillageWorldgen(MinecraftServer server) {
		for (var entry : VANILLA_VILLAGES.entrySet()) {
			registerVillage(server, entry.getKey(), entry.getValue());
		}
	}
}
