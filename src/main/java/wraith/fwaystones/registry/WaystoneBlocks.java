package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.AbstractWaystoneBlock;
import wraith.fwaystones.block.SmallWaystoneBlock;
import wraith.fwaystones.block.WayplateBlock;
import wraith.fwaystones.block.WaystoneBlock;

public final class WaystoneBlocks {

    public static final AbstractWaystoneBlock WAYSTONE = register("waystone", new WaystoneBlock(AbstractBlock.Settings.create().requiresTool().strength(FabricWaystones.CONFIG.waystoneBlockHardness(), 3600000)));
    public static final AbstractWaystoneBlock WAYSTONE_SMALL = register("small_waystone", new SmallWaystoneBlock(AbstractBlock.Settings.copy(WAYSTONE)));
    public static final AbstractWaystoneBlock WAYPLATE = register("wayplate", new WayplateBlock(AbstractBlock.Settings.copy(WAYSTONE)));

    public static final RegistryEntry<AbstractWaystoneBlock> DEFAULT_ENTRY = (RegistryEntry<AbstractWaystoneBlock>) (Object) Registries.BLOCK.getEntry(WAYSTONE);

    public static void init() {}

    private static <T extends Block> T register(String id, T block) {
        Registry.register(Registries.BLOCK, FabricWaystones.id(id), block);
        TagInjector.inject(Registries.BLOCK, FabricWaystones.CONFIG.waystoneBlockMiningTag(), block);
        return block;
    }
}
