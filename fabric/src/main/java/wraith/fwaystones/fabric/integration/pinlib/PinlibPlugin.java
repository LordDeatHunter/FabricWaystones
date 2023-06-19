package wraith.fwaystones.fabric.integration.pinlib;

import com.rokoblox.pinlib.PinLib;
import com.rokoblox.pinlib.mapmarker.MapMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.registry.BlockRegister;
import wraith.fwaystones.util.Utils;

public class PinlibPlugin {
	public static final MapMarker WAYSTONE_MAP_MARKER = PinLib.createDynamicMarker(Utils.ID("waystone"));

	public static long getMarkerColor(BlockGetter world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state != null)
			return switch (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()) {
				// Always put 'L' at the end of numbers so that they are longs NOT integers and 'FF' at the start so alpha is 255.
				case "desert_waystone" -> 0xFFE3DBB0L;
				case "stone_brick_waystone" -> 0xFFB4B2ACL;
				case "red_desert_waystone" -> 0xFFED904AL;
				case "nether_brick_waystone" -> 0xFF59383EL;
				case "red_nether_brick_waystone" -> 0xFF942E31L;
				case "end_stone_brick_waystone" -> 0xFFFBFFE8L;
				case "deepslate_brick_waystone" -> 0xFF808080L;
				case "blackstone_brick_waystone" -> 0xFF4E4B54L;
				default -> 0xFFFFFFFFL;
			};
		return 0xFFFFFFFFL;
	}

	public static void init() {
		BlockRegister.BLOCK_REGISTRY.forEach((block) -> PinLib.registerMapMarkedBlock(
				block.get(),
				() -> WAYSTONE_MAP_MARKER,
				PinlibPlugin::getMarkerColor,
				PinlibPlugin::getMarkerColor,
				PinlibPlugin::getDisplayName
		));
	}

	public static Component getDisplayName(BlockGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof WaystoneBlockEntity waystoneBlockEntity) {
			return Component.literal(waystoneBlockEntity.getWaystoneName());
		}
		return null;
	}

	public static boolean tryUseOnMarkableBlock(ItemStack stack, Level level, BlockPos openPos) {
		return PinLib.tryUseOnMarkableBlock(stack, level, openPos);
	}
}
