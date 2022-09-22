package wraith.fwaystones.integration.pinlib;

import com.rokoblox.pinlib.PinLib;
import com.rokoblox.pinlib.mapmarker.MapMarker;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.registry.BlockRegistry;
import wraith.fwaystones.util.Utils;

public class PinlibPlugin {

    public static final MapMarker WAYSTONE_MAP_MARKER = PinLib.createDynamicMarker(Utils.ID("waystone"));

    public static long getMarkerColor(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state != null)
            return switch (Registry.BLOCK.getId(state.getBlock()).getPath()) {
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
        BlockRegistry.WAYSTONE_BLOCKS.forEach((id, block) -> PinLib.registerMapMarkedBlock(
            block,
            () -> WAYSTONE_MAP_MARKER,
            PinlibPlugin::getMarkerColor,
            PinlibPlugin::getDisplayName
        ));
    }

    public static Text getDisplayName(BlockView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof WaystoneBlockEntity waystoneBlockEntity) {
            return Text.literal(waystoneBlockEntity.getWaystoneName());
        }
        return null;
    }

    public static boolean tryUseOnMarkableBlock(ItemStack stack, World world, BlockPos openPos) {
        return PinLib.tryUseOnMarkableBlock(stack, world, openPos);
    }
}
