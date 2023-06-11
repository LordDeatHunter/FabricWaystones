package wraith.fwaystones.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.item.*;

public final class ItemRegister {
	public static final CreativeTabRegistry.TabSupplier WAYSTONE_GROUP = CreativeTabRegistry.create(new ResourceLocation(Waystones.MOD_ID, Waystones.MOD_ID), () -> new ItemStack(ItemRegister.WAYSTONE.get()));
	public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registries.ITEM);
	// ------------------------------------------------------------------------------------------------------
	public static final RegistrySupplier<Item> WAYSTONE = registerWaystone(BlockRegister.WAYSTONE, "waystone");
	public static final RegistrySupplier<Item> DESERT_WAYSTONE = registerWaystone(BlockRegister.DESERT_WAYSTONE, "desert_waystone");
	public static final RegistrySupplier<Item> RED_DESERT_WAYSTONE = registerWaystone(BlockRegister.RED_DESERT_WAYSTONE, "red_desert_waystone");
	public static final RegistrySupplier<Item> STONE_BRICK_WAYSTONE = registerWaystone(BlockRegister.STONE_BRICK_WAYSTONE, "stone_brick_waystone");
	public static final RegistrySupplier<Item> NETHER_BRICK_WAYSTONE = registerWaystone(BlockRegister.NETHER_BRICK_WAYSTONE, "nether_brick_waystone");
	public static final RegistrySupplier<Item> RED_NETHER_BRICK_WAYSTONE = registerWaystone(BlockRegister.RED_NETHER_BRICK_WAYSTONE, "red_nether_brick_waystone");
	public static final RegistrySupplier<Item> ENDSTONE_BRICK_WAYSTONE = registerWaystone(BlockRegister.ENDSTONE_BRICK_WAYSTONE, "end_stone_brick_waystone");
	public static final RegistrySupplier<Item> DEEPSLATE_BRICK_WAYSTONE = registerWaystone(BlockRegister.DEEPSLATE_BRICK_WAYSTONE, "deepslate_brick_waystone");
	public static final RegistrySupplier<Item> BLACKSTONE_BRICK_WAYSTONE = registerWaystone(BlockRegister.BLACKSTONE_BRICK_WAYSTONE, "blackstone_brick_waystone");
	// ------------------------------------------------------------------------------------------------------
	public static final RegistrySupplier<Item> POCKET_WORMHOLE = ITEM_REGISTRY.register("pocket_wormhole", ()->new PocketWormholeItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1).fireResistant()));
	public static final RegistrySupplier<Item> ABYSS_WATCHER = ITEM_REGISTRY.register("abyss_watcher", ()->new AbyssWatcherItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(4).fireResistant()));
	public static final RegistrySupplier<Item> WAYSTONE_SCROLL = ITEM_REGISTRY.register("waystone_scroll", ()->new WaystoneScrollItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1)));
	public static final RegistrySupplier<Item> LOCAL_VOID = ITEM_REGISTRY.register("local_void", ()->new LocalVoidItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1)));
	public static final RegistrySupplier<Item> VOID_TOTEM = ITEM_REGISTRY.register("void_totem", ()->new VoidTotem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1).rarity(Rarity.UNCOMMON)));
	public static final RegistrySupplier<Item> SCROLL_OF_INFINITE_KNOWLEDGE = ITEM_REGISTRY.register("scroll_of_infinite_knowledge", ()->new ScrollOfInfiniteKnowledgeItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1).fireResistant()));
	public static final RegistrySupplier<Item> WAYSTONE_DEBUGGER = ITEM_REGISTRY.register("waystone_debugger", ()->new WaystoneDebuggerItem(new Item.Properties().arch$tab(WAYSTONE_GROUP).stacksTo(1).fireResistant().rarity(Rarity.EPIC)));
	//-------------------------------------------------------------------------------------------------------
	private static RegistrySupplier<Item> registerWaystone(RegistrySupplier<Block> block, String name) {
		return ITEM_REGISTRY.register(name, ()->new WaystoneItem(block.get(), new Item.Properties().arch$tab(WAYSTONE_GROUP)));
	}
}
