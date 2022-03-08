package wraith.waystones.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.*;
import net.minecraft.util.registry.Registry;
import wraith.waystones.block.WaystoneStyle;
import wraith.waystones.util.Utils;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;

import java.util.HashMap;

public final class BlockRegistry {

    public static final Block WAYSTONE = new WaystoneBlock(style(Blocks.STONE_BUTTON, Blocks.ANDESITE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(style(Blocks.BIRCH_BUTTON, Blocks.SANDSTONE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.STONE_BUTTON, Blocks.STONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(style(Blocks.ACACIA_BUTTON, Blocks.RED_SANDSTONE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.CRIMSON_BUTTON, Blocks.RED_NETHER_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.CRIMSON_BUTTON, Blocks.NETHER_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.BIRCH_BUTTON, Blocks.END_STONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();

    public static void registerBlocks() {
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
        Registry.register(Registry.BLOCK, Utils.ID(id), block);
    }

    private static WaystoneStyle style(Block top, Block bottom) {
        return WaystoneStyle.simple((AbstractButtonBlock) top, (WallBlock) bottom);
    }

}
