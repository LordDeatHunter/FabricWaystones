package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.*;
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
        registerItem("waystone", new WaystoneItem(BlockRegistry.WAYSTONE, new FabricItemSettings()));
        registerItem("desert_waystone", new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new FabricItemSettings()));
        registerItem("red_desert_waystone", new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new FabricItemSettings()));
        registerItem("stone_brick_waystone", new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("nether_brick_waystone", new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("red_nether_brick_waystone", new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("end_stone_brick_waystone", new WaystoneItem(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("deepslate_brick_waystone", new WaystoneItem(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("blackstone_brick_waystone", new WaystoneItem(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, new FabricItemSettings()));
        registerItem("pocket_wormhole", new PocketWormholeItem(new FabricItemSettings().maxCount(1).fireproof()));
        registerItem("abyss_watcher", new AbyssWatcherItem(new FabricItemSettings().maxCount(4).fireproof()));
        registerItem("waystone_scroll", new WaystoneScrollItem(new FabricItemSettings().maxCount(1)));
        registerItem("local_void", new LocalVoidItem(new FabricItemSettings().maxCount(1)));
        registerItem("void_totem", new VoidTotem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)));
        registerItem("scroll_of_infinite_knowledge", new ScrollOfInfiniteKnowledgeItem(new FabricItemSettings().maxCount(1).fireproof()));
        registerItem("waystone_debugger", new WaystoneDebuggerItem(new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC)));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
