package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.WaystoneItem;
import wraith.fwaystones.util.Utils;
import java.util.HashMap;

public final class ItemRegistry {

    private static final HashMap<String, Item> ITEMS = new HashMap<>();
    public static final ItemGroup WAYSTONE_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(BlockRegistry.WAYSTONE)).displayName(Text.translatable("itemGroup.fwaystones.fwaystones")).entries((enabledFeatures, entries) -> ITEMS.values().stream().map(ItemStack::new).forEach(entries::add)).build();

    private ItemRegistry() {}

    private static void registerItem(String id, Item item) {
        ITEMS.put(id, Registry.register(Registries.ITEM, Utils.ID(id), item));
    }

    public static void init() {
        if (!ITEMS.isEmpty()) {
            return;
        }
        Registry.register(Registries.ITEM_GROUP, Utils.ID(FabricWaystones.MOD_ID), WAYSTONE_GROUP);
        registerItem("waystone", new WaystoneItem(BlockRegistry.WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("desert_waystone", new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "desert_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("red_desert_waystone", new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_desert_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("stone_brick_waystone", new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "stone_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("nether_brick_waystone", new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "nether_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("red_nether_brick_waystone", new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "red_nether_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("end_stone_brick_waystone", new WaystoneItem(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "end_stone_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("deepslate_brick_waystone", new WaystoneItem(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "deepslate_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("blackstone_brick_waystone", new WaystoneItem(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "blackstone_brick_waystone"))).useBlockPrefixedTranslationKey()));
        registerItem("pocket_wormhole", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "pocket_wormhole"))).maxCount(1).fireproof().translationKey("item.fwaystones.pocket_wormhole")));
        registerItem("abyss_watcher", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "abyss_watcher"))).maxCount(4).fireproof().translationKey("item.fwaystones.abyss_watcher")));
        registerItem("waystone_scroll", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_scroll"))).maxCount(1).translationKey("item.fwaystones.empty_scroll")));
        registerItem("local_void", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "local_void"))).maxCount(1).translationKey("item.fwaystones.local_void")));
        registerItem("void_totem", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "void_totem"))).maxCount(1).translationKey("item.fwaystones.void_totem")));
        registerItem("scroll_of_infinite_knowledge", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "scroll_of_infinite_knowledge"))).maxCount(1).fireproof().translationKey("item.fwaystones.scroll_of_infinite_knowledge")));
        registerItem("waystone_debugger", new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("fwaystones", "waystone_debugger"))).maxCount(1).fireproof().translationKey("item.fwaystones.waystone_debugger")));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
