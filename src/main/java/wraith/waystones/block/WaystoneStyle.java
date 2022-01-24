package wraith.waystones.block;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.block.enums.WallShape;

public record WaystoneStyle(BlockState upper, BlockState lower, BlockState upperWater, BlockState lowerWater) {
    public static WaystoneStyle simple(AbstractButtonBlock top, WallBlock bottom) {
        var baseTop = top.getDefaultState().with(AbstractButtonBlock.FACE, WallMountLocation.FLOOR);
        var baseBottom = bottom.getDefaultState()
                .with(WallBlock.UP, true)
                .with(WallBlock.EAST_SHAPE, WallShape.LOW)
                .with(WallBlock.WEST_SHAPE, WallShape.LOW)
                .with(WallBlock.NORTH_SHAPE, WallShape.LOW)
                .with(WallBlock.SOUTH_SHAPE, WallShape.LOW);

        return new WaystoneStyle(baseTop, baseBottom, baseTop, baseBottom.with(WallBlock.WATERLOGGED, true));
    }
}
