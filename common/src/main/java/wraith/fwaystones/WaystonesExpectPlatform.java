package wraith.fwaystones;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WaystonesExpectPlatform {
	@ExpectPlatform
	public static boolean pinlibTryUseOnMarkableBlock(ItemStack item, Level level, BlockPos openPos) {
		throw new AssertionError();
	}
}
