package wraith.fwaystones.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.Waystones;

import java.util.Iterator;

public final class TabRegistry {
	public static final CreativeTabRegistry.TabSupplier WAYSTONE_GROUP = CreativeTabRegistry.create(new ResourceLocation(Waystones.MOD_ID, Waystones.MOD_ID), () -> new ItemStack(ItemRegistry.WAYSTONE.get()));
	//-------------------------------------------------------------------------------------------------------
	public static void register(){
		for (Iterator<RegistrySupplier<Item>> item = ItemRegistry.ITEM_REGISTRY.iterator(); item.hasNext(); ) {
			CreativeTabRegistry.append(WAYSTONE_GROUP, item.next());
		}
	}
}
