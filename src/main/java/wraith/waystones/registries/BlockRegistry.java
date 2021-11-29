package wraith.waystones.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.registry.Registry;
import wraith.waystones.util.Utils;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.util.Config;

@SuppressWarnings("removal")
public final class BlockRegistry {

    // TODO: migrate from "breakByTool"
    public static final Block WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));
    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, Config.getInstance().getMiningLevel()).strength(Config.getInstance().getHardness(), 3600000));

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

}
