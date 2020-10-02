package wraith.waystones.registries;

import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.waystones.*;

public class ItemRegistry {

    public static final Item WAYSTONE = new BlockItem(BlockRegistry.WAYSTONE, new Item.Settings().group(CustomItemGroup.WAYSTONE_GROUP));

    public static void registerItems(){
        Registry.register(Registry.ITEM, new Identifier(Waystones.MOD_ID, "waystone"), WAYSTONE);
    }

}
