package wraith.waystones.registries;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import wraith.waystones.CustomItemGroup;
import wraith.waystones.Utils;
import wraith.waystones.items.AbyssWatcherItem;
import wraith.waystones.items.PocketWormholeItem;
import wraith.waystones.items.WaystoneScroll;

import java.util.HashMap;

public final class ItemRegistry {

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void registerItems(){
        for (String id : ITEMS.keySet()) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

    static {
        ITEMS.put("waystone", new BlockItem(BlockRegistry.WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("pocket_wormhole", new PocketWormholeItem(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        ITEMS.put("abyss_watcher", new AbyssWatcherItem(new Item.Settings().maxCount(4).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        ITEMS.put("empty_scroll", new Item(new Item.Settings().maxCount(16).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("waystone_scroll", new WaystoneScroll(new Item.Settings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
    }

}
