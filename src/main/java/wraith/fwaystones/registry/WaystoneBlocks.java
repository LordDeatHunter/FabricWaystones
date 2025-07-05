package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneShape;

import java.util.HashMap;

public final class WaystoneBlocks {

    public static final Block WAYSTONE = WaystoneBlock.of(createBaseSettings().mapColor(MapColor.STONE_GRAY), WaystoneShape.NORMAL);
    public static final Block WAYSTONE_SMALL = WaystoneBlock.of(createBaseSettings().mapColor(MapColor.STONE_GRAY), WaystoneShape.SMALL);
    public static final Block WAYSTONE_MINI = WaystoneBlock.of(createBaseSettings().mapColor(MapColor.STONE_GRAY), WaystoneShape.MINI);

    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();

    public static AbstractBlock.Settings createBaseSettings() {
        return AbstractBlock.Settings.create().requiresTool().strength(FabricWaystones.CONFIG.waystoneBlockHardness(), 3600000);
    }

    public static void init() {
        registerAndAdd("waystone", WAYSTONE);
        registerAndAdd("waystone_small", WAYSTONE_SMALL);
        registerAndAdd("waystone_mini", WAYSTONE_MINI);
    }

    private static void registerAndAdd(String id, Block block) {
        WAYSTONE_BLOCKS.put(id, block);
        Registry.register(Registries.BLOCK, FabricWaystones.id(id), block);
        TagInjector.inject(Registries.BLOCK, FabricWaystones.CONFIG.waystoneBlockMiningTag(), block);
    }

}
