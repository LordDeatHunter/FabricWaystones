package wraith.waystones.compat;

import com.telepathicgrunt.repurposedstructures.RepurposedStructures;
import com.telepathicgrunt.repurposedstructures.world.structures.pieces.StructurePiecesBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import wraith.waystones.Waystones;
import wraith.waystones.util.WaystonesWorldgen;

import java.util.HashMap;

public final class RepurposedStructuresCompat {

    private RepurposedStructuresCompat() {
    }

    public static final HashMap<Identifier, Identifier> REPURPOSED_STRUCTURES_VILLAGES = new HashMap<>();


    public static void registerRSVillages(MinecraftServer server) {
        Waystones.LOGGER.info("[Repurposed Structures] Detected!");
        for (var entry : REPURPOSED_STRUCTURES_VILLAGES.entrySet()) {
            WaystonesWorldgen.registerVillage(server, entry.getKey(), entry.getValue());
        }
    }

    public static void init() {
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:village_waystone"), 1);
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:red_desert_village_waystone"), 1);
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:stone_brick_village_waystone"), 1);
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:mossy_stone_brick_village_waystone"), 1);
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:nether_brick_waystone"), 1);
        StructurePiecesBehavior.PIECES_COUNT.put(new Identifier("waystones:red_nether_brick_waystone"), 1);

        StructurePiecesBehavior.addRequiredVillagePiece("village_badlands", new Identifier("waystones:red_desert_village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.badlandsVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_birch", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.birchVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_crimson", new Identifier("waystones:red_nether_brick_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.crimsonVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_dark_forest", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.darkForestVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_giant_taiga", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.giantTaigaVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_jungle", new Identifier("waystones:mossy_stone_brick_village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.jungleVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_mountains", new Identifier("waystones:stone_brick_village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.mountainsVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_mushroom", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.mushroomVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_oak", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.oakVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_swamp", new Identifier("waystones:village_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.swampVillageSize);
        StructurePiecesBehavior.addRequiredVillagePiece("village_warped", new Identifier("waystones:nether_brick_waystone"), RepurposedStructures.RSAllConfig.RSVillagesConfig.size.warpedVillageSize);
    }

    static {
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/badlands/houses"), new Identifier("waystones:red_desert_village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/birch/houses"), new Identifier("waystones:village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/crimson/houses"), new Identifier("waystones:red_nether_brick_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/dark_forest/houses"), new Identifier("waystones:village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/giant_taiga/houses"), new Identifier("waystones:village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/jungle/houses"), new Identifier("waystones:mossy_stone_brick_village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/mountains/houses"), new Identifier("waystones:stone_brick_village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/swamp/houses"), new Identifier("waystones:village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/warped/houses"), new Identifier("waystones:nether_brick_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/mushroom/houses"), new Identifier("waystones:village_waystone"));
        REPURPOSED_STRUCTURES_VILLAGES.put(new Identifier("repurposed_structures", "villages/oak/houses"), new Identifier("waystones:village_waystone"));

    }

}
