package wraith.fwaystones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;

public class SmallWaystoneBlock extends AbstractWaystoneBlock {

    public static final VoxelShape COLLISION_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(2, 4, 2, 14, 8, 14),
        Block.createCuboidShape(3, 2, 3, 13, 4, 13),
        Block.createCuboidShape(3, 8, 3, 13, 10, 13),
        Block.createCuboidShape(2, 1, 2, 14, 2, 14),
        Block.createCuboidShape(1, 0, 1, 15, 1, 15)
    );

    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
        COLLISION_SHAPE,
        Block.createCuboidShape(7, 10, 12, 9, 13, 13),
        Block.createCuboidShape(7, 10, 3, 9, 13, 4),
        Block.createCuboidShape(7, 8, 13, 9, 11, 15),
        Block.createCuboidShape(7, 8, 1, 9, 11, 3),
        Block.createCuboidShape(13, 8, 7, 15, 11, 9),
        Block.createCuboidShape(1, 8, 7, 3, 11, 9),
        Block.createCuboidShape(12, 10, 7, 13, 13, 9),
        Block.createCuboidShape(3, 10, 7, 4, 13, 9)
    );

    public SmallWaystoneBlock(Settings settings) {
        super(10, settings);
    }

    public List<Vec3i> getTeleportOffsets(Direction direction) {
        var baseOffsets = getHorizontalTeleportOffsets(direction);

        var returned = new ArrayList<>(baseOffsets);
        returned.addAll(baseOffsets.stream().map(Vec3i::down).toList());
        returned.add(Vec3i.ZERO);
        return returned;
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
