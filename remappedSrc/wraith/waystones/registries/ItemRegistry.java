package wraith.waystones.registries;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import wraith.waystones.CustomItemGroup;
import wraith.waystones.Utils;
import wraith.waystones.item.PocketWormholeItem;
import wraith.waystones.item.WaystoneScroll;

import java.util.HashMap;

public class ItemRegistry {

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void addItems() {
        ITEMS.put("waystone", new BlockItem(BlockRegistry.WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("pocket_wormhole", new PocketWormholeItem(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("empty_scroll", new Item(new Item.Settings().maxCount(16).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("waystone_scroll", new WaystoneScroll(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("abyss_watcher", new WaystoneScroll(new Item.Settings()));
    }

    public static void registerItems(){
        for (String id : ITEMS.keySet()) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

}
