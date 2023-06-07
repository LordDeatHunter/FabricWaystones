package wraith.fwaystones.registry;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.item.WaystoneScrollItem;
import wraith.fwaystones.util.Utils;

@Environment(EnvType.CLIENT)
public class ModelProviderReg {
	public static void register(){
		ItemPropertiesRegistry.registerGeneric(Utils.ID("has_learned"),
				(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int seed) -> {
					if (itemStack.isEmpty()) {
						return 0f;
					}
					if (itemStack.getItem() instanceof WaystoneScrollItem) {
						CompoundTag tag = itemStack.getTag();
						return tag == null || !tag.contains(Waystones.MOD_ID) || tag.getList(Waystones.MOD_ID, Tag.TAG_STRING).isEmpty() ? 0 : 1;
					} else if (itemStack.getItem() instanceof LocalVoidItem) {
						CompoundTag tag = itemStack.getTag();
						return tag == null || !tag.contains(Waystones.MOD_ID) ? 0 : 1;
					}
					return 0f;
				}
		);
	}
}
