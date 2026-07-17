package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import wraith.fwaystones.item.*;

import java.util.HashMap;

public final class ItemRegistry {

    private static final HashMap<String, Item> ITEMS = new HashMap<>();
    public static final ResourceKey<CreativeModeTab> WAYSTONE_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath("fwaystones", "waystones"));
    public static final CreativeModeTab WAYSTONE_GROUP = FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(BlockRegistry.WAYSTONE))
            .title(Component.translatable("itemGroup.fwaystones.fwaystones"))
            .build();

    private ItemRegistry() {}

    private static void registerItem(Item item, ResourceKey<Item> registryKey) {
        Item registeredItem = Registry.register(BuiltInRegistries.ITEM, registryKey.identifier(), item);
        ITEMS.put(registryKey.identifier().getPath(), registeredItem);
    }

    public static void init() {
        if (!ITEMS.isEmpty()) {
            return;
        }

        // Block Items
        registerItem(new WaystoneItem(BlockRegistry.WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone")));
        registerItem(new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "desert_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "desert_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "red_desert_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "red_desert_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "stone_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "stone_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "nether_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "nether_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "red_nether_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "red_nether_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "end_stone_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "end_stone_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "deepslate_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "deepslate_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "blackstone_brick_waystone"))).useBlockDescriptionPrefix()), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "blackstone_brick_waystone")));

        // Items
        registerItem(new PocketWormholeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "pocket_wormhole"))).stacksTo(1).fireResistant().overrideDescription("item.fwaystones.pocket_wormhole")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "pocket_wormhole")));
        registerItem(new AbyssWatcherItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "abyss_watcher"))).stacksTo(4).fireResistant().overrideDescription("item.fwaystones.abyss_watcher")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "abyss_watcher")));
        registerItem(new WaystoneScrollItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone_scroll"))).stacksTo(1).overrideDescription("item.fwaystones.empty_scroll")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone_scroll")));
        registerItem(new LocalVoidItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "local_void"))).stacksTo(1).overrideDescription("item.fwaystones.local_void")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "local_void")));
        registerItem(new VoidTotem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "void_totem"))).stacksTo(1).overrideDescription("item.fwaystones.void_totem")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "void_totem")));
        registerItem(new ScrollOfInfiniteKnowledgeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "scroll_of_infinite_knowledge"))).stacksTo(1).fireResistant().overrideDescription("item.fwaystones.scroll_of_infinite_knowledge")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "scroll_of_infinite_knowledge")));
        registerItem(new WaystoneDebuggerItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone_debugger"))).stacksTo(1).fireResistant().overrideDescription("item.fwaystones.waystone_debugger")), ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fwaystones", "waystone_debugger")));

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, WAYSTONE_GROUP_KEY, WAYSTONE_GROUP);
        CreativeModeTabEvents.modifyOutputEvent(WAYSTONE_GROUP_KEY)
                .register((output) -> output.acceptAll(ITEMS.values().stream().map(ItemStack::new).toList()));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
