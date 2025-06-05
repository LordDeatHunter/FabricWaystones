package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;

import java.util.HashMap;

public final class WaystoneBlocks {

    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));
    public static final Block WAYSTONE = new WaystoneBlock(createBaseSettings().mapColor(MapColor.STONE_GRAY));

    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();

    private static Identifier miningLevelTag;

    public static AbstractBlock.Settings createBaseSettings() {
        return AbstractBlock.Settings.create().requiresTool().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000);
    }

    public static void init() {
        var miningLevel = FabricWaystones.CONFIG.waystone_block_required_mining_level();

        miningLevelTag = Identifier.of(switch (miningLevel) {
            case 1 -> "minecraft:needs_stone_tool";
            case 2 -> "minecraft:needs_iron_tool";
            case 3 -> "minecraft:needs_diamond_tool";
            default -> "fabric:needs_tool_level_" + miningLevel;
        });

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
        TagInjector.inject(Registries.BLOCK, miningLevelTag, block);
    }

}
