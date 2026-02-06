package wraith.fwaystones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.mixin.TallBlantBlockAccessor;

import java.util.ArrayList;
import java.util.List;

public class WaystoneBlock extends AbstractWaystoneBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public static final VoxelShape BOTTOM_COLLISION_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(2, 2, 2, 14, 5, 14),
        Block.createCuboidShape(1, 0, 1, 15, 2, 15),
        Block.createCuboidShape(3, 5, 3, 13, 16, 13)
    );

    public static final VoxelShape TOP_COLLISION_SHAPE = VoxelShapes.union(
        Block.createCuboidShape(2, 1, 2, 14, 5, 14),
        Block.createCuboidShape(3, 0, 3, 13, 1, 13),
        Block.createCuboidShape(3, 5, 3, 13, 7, 13)
    );

    public static final VoxelShape TOP_OUTLINE_SHAPE = VoxelShapes.union(
        TOP_COLLISION_SHAPE,
        Block.createCuboidShape(7, 7, 12, 9, 10, 13),
        Block.createCuboidShape(7, 7, 3, 9, 10, 4),
        Block.createCuboidShape(7, 5, 13, 9, 8, 15),
        Block.createCuboidShape(7, 5, 1, 9, 8, 3),
        Block.createCuboidShape(13, 5, 7, 15, 8, 9),
        Block.createCuboidShape(1, 5, 7, 3, 8, 9),
        Block.createCuboidShape(12, 7, 7, 13, 10, 9),
        Block.createCuboidShape(3, 7, 7, 4, 10, 9)
    );

    public WaystoneBlock(Settings settings) {
        super(23, settings);
        this.setDefaultState(
            this.getDefaultState()
                .with(HALF, DoubleBlockHalf.LOWER)
        );
    }

    public List<Vec3i> getTeleportOffsets(Direction direction) {
        var baseOffsets = getHorizontalTeleportOffsets(direction);

        var returned = new ArrayList<>(baseOffsets);
        returned.addAll(baseOffsets.stream().map(Vec3i::down).toList());
        returned.addAll(baseOffsets.stream().map(Vec3i::up).toList());
        returned.add(Vec3i.ZERO.up());
        return returned;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? TOP_OUTLINE_SHAPE : BOTTOM_COLLISION_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? TOP_COLLISION_SHAPE : BOTTOM_COLLISION_SHAPE;
    }

    @Override
    public BlockPos getBasePos(BlockPos pos, BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
    }

    @Override
    public void scheduleBlockRerender(World world, BlockPos pos) {
        world.scheduleBlockRerenderIfNeeded(pos.up(), Blocks.AIR.getDefaultState(), world.getBlockState(pos.up()));
        super.scheduleBlockRerender(world, pos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var pos = ctx.getBlockPos();
        var world = ctx.getWorld();
        if (pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx)) {
            var state = super.getPlacementState(ctx);
            if (state != null) return state.with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
            TallBlantBlockAccessor.fWaystones$onBreakInCreative(world, pos, state, player);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HALF);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        var half = state.get(HALF);

        if (direction.getAxis() != Direction.Axis.Y || half == DoubleBlockHalf.LOWER != (direction == Direction.UP)) {
            return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        } else {
            return neighborState.isOf(this) && neighborState.get(HALF) != half
                ? neighborState.with(HALF, half)
                : Blocks.AIR.getDefaultState();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockPos newPos;
        DoubleBlockHalf verticalPosition;

        if (state.isOf(this)) {
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                newPos = pos.down();
                verticalPosition = DoubleBlockHalf.LOWER;
            } else {
                newPos = pos.up();
                verticalPosition = DoubleBlockHalf.UPPER;
            }

            if (!(newState.isOf(this))) {
                var waystone = getEntity(world, pos);

                if (waystone != null) {
                    if (!world.isClient) {
                        var uuid = waystone.getUUID();
                        if (uuid != null) WaystoneDataStorage.getStorage(world).removePosition(uuid);
                    }

                    world.removeBlockEntity(waystone.getPos());
                }

                if (newPos != null) world.setBlockState(newPos, newState);
            } else if (newPos != null) {
                var fluid = world.getFluidState(newPos).getFluid() == Fluids.WATER && verticalPosition == DoubleBlockHalf.LOWER;

                var adjustedState = newState.with(WATERLOGGED, fluid)
                        .with(HALF, verticalPosition);

                world.setBlockState(newPos, adjustedState);
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !state.contains(HALF)
               || state.get(HALF) == DoubleBlockHalf.LOWER
               || isCorrectOtherHalf(state, world.getBlockState(pos.down()));
    }

    protected boolean isCorrectOtherHalf(BlockState state, BlockState other) {
        return state.isOf(this) && other.isOf(this) &&
               state.get(HALF).getOtherHalf().equals(other.get(HALF)) &&
               state.get(FACING).equals(other.get(FACING)) &&
               state.get(GENERATED).equals(other.get(GENERATED));
    }
}
