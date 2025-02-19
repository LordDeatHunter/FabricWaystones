package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fwaystones.item.WaystoneItem;
import java.util.HashMap;

public final class ItemRegistry {

    private static final HashMap<String, Item> ITEMS = new HashMap<>();
    public static final RegistryKey<ItemGroup> WAYSTONE_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of("fwaystones", "waystones"));
    public static final ItemGroup WAYSTONE_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BlockRegistry.WAYSTONE))
            .displayName(Text.translatable("itemGroup.fwaystones.fwaystones"))
            .build();

    private ItemRegistry() {}

    private static void registerItem(Item item, RegistryKey<Item> registryKey) {
        Item registeredItem = Registry.register(Registries.ITEM, registryKey.getValue(), item);
        ITEMS.put(registryKey.getValue().getPath(), registeredItem);
    }

    public static void init() {
        if (!ITEMS.isEmpty()) {
            return;
        }

        // Block Items
        registerItem(new WaystoneItem(BlockRegistry.WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone")));
        registerItem(new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "desert_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "desert_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_desert_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_desert_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "stone_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "stone_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "nether_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "nether_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_nether_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_nether_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "end_stone_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "end_stone_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "deepslate_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "deepslate_brick_waystone")));
        registerItem(new WaystoneItem(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "blackstone_brick_waystone"))).useBlockPrefixedTranslationKey()), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "blackstone_brick_waystone")));

        // Items
        registerItem(new PocketWormholeItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "pocket_wormhole"))).maxCount(1).fireproof().translationKey("item.fwaystones.pocket_wormhole")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "pocket_wormhole")));
        registerItem(new AbyssWatcherItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "abyss_watcher"))).maxCount(4).fireproof().translationKey("item.fwaystones.abyss_watcher")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "abyss_watcher")));
        registerItem(new WaystoneScrollItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_scroll"))).maxCount(1).translationKey("item.fwaystones.empty_scroll")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_scroll")));
        registerItem(new LocalVoidItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "local_void"))).maxCount(1).translationKey("item.fwaystones.local_void")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "local_void")));
        registerItem(new VoidTotem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "void_totem"))).maxCount(1).translationKey("item.fwaystones.void_totem")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "void_totem")));
        registerItem(new ScrollOfInfiniteKnowledgeItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "scroll_of_infinite_knowledge"))).maxCount(1).fireproof().translationKey("item.fwaystones.scroll_of_infinite_knowledge")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "scroll_of_infinite_knowledge")));
        registerItem(new WaystoneDebuggerItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_debugger"))).maxCount(1).fireproof().translationKey("item.fwaystones.waystone_debugger")), RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_debugger")));

        Registry.register(Registries.ITEM_GROUP, WAYSTONE_GROUP_KEY, WAYSTONE_GROUP);
        ItemGroupEvents.modifyEntriesEvent(WAYSTONE_GROUP_KEY)
                .register((group) -> group.addAll(ITEMS.values().stream().map(ItemStack::new).toList()));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
