package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.SmallWaystoneBlock;
import wraith.fwaystones.block.WayplateBlock;
import wraith.fwaystones.block.WaystoneBlock;

public final class WaystoneBlocks {

    public static final Block WAYSTONE = register("waystone", new WaystoneBlock(AbstractBlock.Settings.create().requiresTool().strength(FabricWaystones.CONFIG.waystoneBlockHardness(), 3600000)));
    public static final Block WAYSTONE_SMALL = register("small_waystone", new SmallWaystoneBlock(AbstractBlock.Settings.copy(WAYSTONE)));
    public static final Block WAYPLATE = register("wayplate", new WayplateBlock(AbstractBlock.Settings.copy(WAYSTONE)));

    public static void init() {}

    private static Block register(String id, Block block) {
        Registry.register(Registries.BLOCK, FabricWaystones.id(id), block);
        TagInjector.inject(Registries.BLOCK, FabricWaystones.CONFIG.waystoneBlockMiningTag(), block);
        return block;
    }
}
