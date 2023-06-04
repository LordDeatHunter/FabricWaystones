package wraith.fwaystones.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.item.*;

public final class ItemRegistry {
    public static final CreativeModeTab WAYSTONE_GROUP = CreativeTabRegistry.create(new ResourceLocation(Waystones.MOD_ID, Waystones.MOD_ID), () ->
            new ItemStack(ItemRegistry.WAYSTONE.get()));

    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registry.ITEM_REGISTRY);
    // ------------------------------------------------------------------------------------------------------
    public static final RegistrySupplier<Item> WAYSTONE = registerWaystone(BlockRegistry.WAYSTONE, "waystone");
    public static final RegistrySupplier<Item> DESERT_WAYSTONE = registerWaystone(BlockRegistry.DESERT_WAYSTONE, "desert_waystone");
    public static final RegistrySupplier<Item> RED_DESERT_WAYSTONE = registerWaystone(BlockRegistry.RED_DESERT_WAYSTONE, "red_desert_waystone");
    public static final RegistrySupplier<Item> STONE_BRICK_WAYSTONE = registerWaystone(BlockRegistry.STONE_BRICK_WAYSTONE, "stone_brick_waystone");
    public static final RegistrySupplier<Item> NETHER_BRICK_WAYSTONE = registerWaystone(BlockRegistry.NETHER_BRICK_WAYSTONE, "nether_brick_waystone");
    public static final RegistrySupplier<Item> RED_NETHER_BRICK_WAYSTONE = registerWaystone(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, "red_nether_brick_waystone");
    public static final RegistrySupplier<Item> ENDSTONE_BRICK_WAYSTONE = registerWaystone(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, "end_stone_brick_waystone");
    public static final RegistrySupplier<Item> DEEPSLATE_BRICK_WAYSTONE = registerWaystone(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, "deepslate_brick_waystone");
    public static final RegistrySupplier<Item> BLACKSTONE_BRICK_WAYSTONE = registerWaystone(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, "blackstone_brick_waystone");

    public static final RegistrySupplier<Item> POCKET_WORMHOLE = ITEM_REGISTRY.register("pocket_wormhole", ()->new PocketWormholeItem(new Item.Properties().stacksTo(1).fireResistant().tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> ABYSS_WATCHER = ITEM_REGISTRY.register("abyss_watcher", ()->new AbyssWatcherItem(new Item.Properties().stacksTo(4).fireResistant().tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> WAYSTONE_SCROLL = ITEM_REGISTRY.register("waystone_scroll", ()->new WaystoneScrollItem(new Item.Properties().stacksTo(1).tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> LOCAL_VOID = ITEM_REGISTRY.register("local_void", ()->new LocalVoidItem(new Item.Properties().stacksTo(1).tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> VOID_TOTEM = ITEM_REGISTRY.register("void_totem", ()->new VoidTotem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> SCROLL_OF_INFINITE_KNOWLEDGE = ITEM_REGISTRY.register("scroll_of_infinite_knowledge", ()->new ScrollOfInfiniteKnowledgeItem(new Item.Properties().stacksTo(1).fireResistant().tab(WAYSTONE_GROUP)));
    public static final RegistrySupplier<Item> WAYSTONE_DEBUGGER = ITEM_REGISTRY.register("waystone_debugger", ()->new WaystoneDebuggerItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).tab(WAYSTONE_GROUP)));

    //-------------------------------------------------------------------------------------------------------
    private static RegistrySupplier<Item> registerWaystone(RegistrySupplier<Block> block, String name) {
        RegistrySupplier<Item> toReturn = ITEM_REGISTRY.register(name, ()->new WaystoneItem(block.get(), new Item.Properties().tab(WAYSTONE_GROUP)));
        //registerBlockItem(name, toReturn);
        return toReturn;
    }
}
