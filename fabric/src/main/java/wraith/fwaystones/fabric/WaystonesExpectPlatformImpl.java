package wraith.fwaystones.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wraith.fwaystones.fabric.integration.pinlib.PinlibPlugin;

public class WaystonesExpectPlatformImpl {
	public static boolean pinlibTryUseOnMarkableBlock(ItemStack item, Level level, BlockPos openPos) {
		// TODO: Mixin error
		//  return FabricLoader.getInstance().isModLoaded("pinlib") && PinlibPlugin.tryUseOnMarkableBlock(item, level, openPos);
		return false;
	}
}
