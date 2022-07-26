package wraith.fwaystones.registry;

import com.rokoblox.pinlib.PinLib;
import com.rokoblox.pinlib.mapmarker.MapMarker;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.registry.Registry;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.util.Config;
import wraith.fwaystones.util.Utils;

import java.util.HashMap;

public final class BlockRegistry {

    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final Block WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(Config.getInstance().getHardness(), 3600000));
    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();

    // Map marker for waystones using PinLib.
    public static final MapMarker WAYSTONE_MAP_MARKER = PinLib.createDynamicMarker(Utils.ID("waystone"));

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

}
