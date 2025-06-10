package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneType;
import wraith.fwaystones.block.WaystoneBlock;

import java.util.HashMap;

public final class WaystoneBlocks {

    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/blackstone_brick_waystone"),
            0xB4202A
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/deepslate_brick_waystone"),
            0xCF573C
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/desert_waystone"),
            0xF1641F
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/endstone_brick_waystone"),
            0x3B2754
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/nether_brick_waystone"),
            0xC4F129
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/red_desert_waystone"),
            0x74DFED
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/red_nether_brick_waystone"),
            0x67CB37
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/stone_brick_waystone"),
            0xCC00FA
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block WAYSTONE = new WaystoneBlock(
        new WaystoneType(
            FabricWaystones.id("item/waystone"),
            0xC4F129
        ),
        createBaseSettings().mapColor(MapColor.STONE_GRAY));

    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();

    public static AbstractBlock.Settings createBaseSettings() {
        return AbstractBlock.Settings.create().requiresTool().strength(FabricWaystones.CONFIG.waystoneBlockHardness(), 3600000);
    }

    public static void init() {
        registerAndAdd("waystone", WAYSTONE);
        registerAndAdd("desert_waystone", DESERT_WAYSTONE);
        registerAndAdd("stone_brick_waystone", STONE_BRICK_WAYSTONE);
        registerAndAdd("red_desert_waystone", RED_DESERT_WAYSTONE);
        registerAndAdd("nether_brick_waystone", NETHER_BRICK_WAYSTONE);
        registerAndAdd("red_nether_brick_waystone", RED_NETHER_BRICK_WAYSTONE);
        registerAndAdd("end_stone_brick_waystone", ENDSTONE_BRICK_WAYSTONE);
        registerAndAdd("deepslate_brick_waystone", DEEPSLATE_BRICK_WAYSTONE);
        registerAndAdd("blackstone_brick_waystone", BLACKSTONE_BRICK_WAYSTONE);
    }

    private static void registerAndAdd(String id, Block block) {
        WAYSTONE_BLOCKS.put(id, block);
        Registry.register(Registries.BLOCK, FabricWaystones.id(id), block);
        TagInjector.inject(Registries.BLOCK, FabricWaystones.CONFIG.waystoneBlockMiningTag(), block);
    }

}
