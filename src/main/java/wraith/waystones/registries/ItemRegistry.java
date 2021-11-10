package wraith.waystones.registries;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import wraith.waystones.util.CustomItemGroup;
import wraith.waystones.util.Utils;
import wraith.waystones.item.*;

import java.util.HashMap;

public final class ItemRegistry {

    public static final HashMap<String, Item> ITEMS = new HashMap<>();

    public static void registerItems(){
        for (String id : ITEMS.keySet()) {
            Registry.register(Registry.ITEM, Utils.ID(id), ITEMS.get(id));
        }
    }

    static {
        ITEMS.put("waystone_debugger", new WaystoneDebugger(new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC)));
        ITEMS.put("waystone", new WaystoneItem(BlockRegistry.WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("desert_waystone", new WaystoneItem(BlockRegistry.DESERT_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("red_desert_waystone", new WaystoneItem(BlockRegistry.RED_DESERT_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("stone_brick_waystone", new WaystoneItem(BlockRegistry.STONE_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("nether_brick_waystone", new WaystoneItem(BlockRegistry.NETHER_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("red_nether_brick_waystone", new WaystoneItem(BlockRegistry.RED_NETHER_BRICK_WAYSTONE, new FabricItemSettings().group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("pocket_wormhole", new PocketWormholeItem(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        ITEMS.put("abyss_watcher", new AbyssWatcherItem(new FabricItemSettings().maxCount(4).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
        ITEMS.put("waystone_scroll", new WaystoneScroll(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("local_void", new LocalVoid(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP)));
        ITEMS.put("scroll_of_infinite_knowledge", new ScrollOfInfiniteKnowledge(new FabricItemSettings().maxCount(1).group(CustomItemGroup.WAYSTONE_GROUP).fireproof()));
    }

}
