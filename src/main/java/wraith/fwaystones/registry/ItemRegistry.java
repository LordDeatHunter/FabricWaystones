package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import wraith.fwaystones.item.*;
import wraith.fwaystones.util.CustomItemGroup;
import wraith.fwaystones.util.Utils;

import java.util.HashMap;

public final class ItemRegistry {

    private static final HashMap<String, Item> ITEMS = new HashMap<>();

    private ItemRegistry() {}

    private static void registerItem(String id, Item item) {
        ITEMS.put(id, Registry.register(Registry.ITEM, Utils.ID(id), item));
    }

    public static void init() {
        if (!ITEMS.isEmpty()) {
            return;
        }
        registerItem("waystone", new WaystoneItem(BlockRegistry.WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("desert_waystone", new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("red_desert_waystone", new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("stone_brick_waystone", new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("nether_brick_waystone", new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("red_nether_brick_waystone", new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("end_stone_brick_waystone", new WaystoneItem(BlockRegistry.ENDSTONE_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("deepslate_brick_waystone", new WaystoneItem(BlockRegistry.DEEPSLATE_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("blackstone_brick_waystone", new WaystoneItem(BlockRegistry.BLACKSTONE_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("pocket_wormhole", new PocketWormholeItem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("abyss_watcher", new AbyssWatcherItem(new FabricItemSettings().maxCount(4).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("waystone_scroll", new WaystoneScrollItem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("local_void", new LocalVoidItem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("void_totem", new VoidTotem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).rarity(Rarity.UNCOMMON)));
        registerItem("scroll_of_infinite_knowledge", new ScrollOfInfiniteKnowledgeItem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("waystone_debugger", new WaystoneDebuggerItem(new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC)));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
