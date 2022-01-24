package wraith.waystones.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.util.registry.Registry;
import wraith.waystones.block.WaystoneStyle;
import wraith.waystones.util.Utils;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.util.Config;

@SuppressWarnings("removal")
public final class BlockRegistry {

    // TODO: migrate from "breakByTool"
public static final Block WAYSTONE = new WaystoneBlock(style(Blocks.STONE_BUTTON, Blocks.ANDESITE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(style(Blocks.BIRCH_BUTTON, Blocks.SANDSTONE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.STONE_BUTTON, Blocks.STONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(style(Blocks.ACACIA_BUTTON, Blocks.RED_SANDSTONE_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.CRIMSON_BUTTON, Blocks.RED_NETHER_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.CRIMSON_BUTTON, Blocks.NETHER_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.BIRCH_BUTTON, Blocks.END_STONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(style(Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BRICK_WALL), FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));

    public static void registerBlocks() {
        Registry.register(Registry.BLOCK, Utils.ID("waystone"), WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("desert_waystone"), DESERT_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("stone_brick_waystone"), STONE_BRICK_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("red_desert_waystone"), RED_DESERT_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("nether_brick_waystone"), NETHER_BRICK_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("red_nether_brick_waystone"), RED_NETHER_BRICK_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("end_stone_brick_waystone"), ENDSTONE_BRICK_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("deepslate_brick_waystone"), DEEPSLATE_BRICK_WAYSTONE);
        Registry.register(Registry.BLOCK, Utils.ID("blackstone_brick_waystone"), BLACKSTONE_BRICK_WAYSTONE);
    }

    private static WaystoneStyle style(Block top, Block bottom) {
        return WaystoneStyle.simple((AbstractButtonBlock) top, (WallBlock) bottom);
    }

}
