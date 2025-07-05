package wraith.fwaystones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class WayPlateBlock extends AbstractWaystoneBlock {

    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(1, 0, 1, 15, 1, 15),
        Block.createCuboidShape(2, 1, 2, 14, 2, 14),
        Block.createCuboidShape(3, 2, 3, 13, 3, 13)
    );

    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
        COLLISION_SHAPE,
        Block.createCuboidShape(7, 2, 13, 9, 5, 14),
        Block.createCuboidShape(7, 2, 2, 9, 5, 3),
        Block.createCuboidShape(7, 1, 14, 9, 4, 15),
        Block.createCuboidShape(7, 1, 1, 9, 4, 2),
        Block.createCuboidShape(14, 1, 7, 15, 4, 9),
        Block.createCuboidShape(1, 1, 7, 2, 4, 9),
        Block.createCuboidShape(13, 2, 7, 14, 5, 9),
        Block.createCuboidShape(2, 2, 7, 3, 5, 9)
    );

    public WayPlateBlock(Settings settings) {
        super(3, settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }
}
