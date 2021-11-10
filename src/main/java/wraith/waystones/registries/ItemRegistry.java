package wraith.waystones.registries;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import wraith.waystones.item.*;
import wraith.waystones.util.CustomItemGroup;
import wraith.waystones.util.Utils;

import java.util.HashMap;

public final class ItemRegistry {

    private ItemRegistry(){}

    private static final HashMap<String, Item> ITEMS = new HashMap<>();

    private static void registerItem(String id, Item item){
        ITEMS.put(id, Registry.register(Registry.ITEM, Utils.ID(id), item));
    }

    public static void init(){
        if (!ITEMS.isEmpty()) {
            return;
        }
        registerItem("waystone", new WaystoneItem(BlockRegistry.WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("desert_waystone", new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("red_desert_waystone", new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("stone_brick_waystone", new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("nether_brick_waystone", new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("red_nether_brick_waystone", new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("pocket_wormhole", new PocketWormholeItem(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("abyss_watcher", new AbyssWatcherItem(new Item.Settings().maxCount(4).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("waystone_scroll", new WaystoneScroll(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("local_void", new LocalVoid(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        registerItem("scroll_of_infinite_knowledge", new ScrollOfInfiniteKnowledge(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        registerItem("waystone_debugger", new WaystoneDebugger(new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC)));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
