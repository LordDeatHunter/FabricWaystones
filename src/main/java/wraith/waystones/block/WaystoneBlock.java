package wraith.waystones.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import wraith.waystones.Config;
import wraith.waystones.Utils;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;
import wraith.waystones.registries.ItemRegistry;

import java.util.List;

public class WaystoneBlock extends BlockWithEntity {

    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    //BOTTOM
    protected static final VoxelShape vs1_1 = Block.createCuboidShape(1f, 0f, 1f, 15f, 2f, 15f);
    protected static final VoxelShape vs2_1 = Block.createCuboidShape(2f, 2f, 2f, 14f, 5f, 14f);
    protected static final VoxelShape vs3_1 = Block.createCuboidShape(3f, 5f, 3f, 13f, 16f, 13f);
    //TOP
    protected static final VoxelShape vs1_2 = Block.createCuboidShape(3f, 0f, 3f, 13f, 1f, 13f);
    protected static final VoxelShape vs2_2 = Block.createCuboidShape(2f, 1f, 2f, 14f, 5f, 14f);
    protected static final VoxelShape vs3_2 = Block.createCuboidShape(3f, 5f, 3f, 13f, 7f, 13f);
    protected static final VoxelShape vs4_2 = Block.createCuboidShape(7f, 5f, 1f, 9f, 8f, 3f);
    protected static final VoxelShape vs5_2 = Block.createCuboidShape(7f, 7f, 3f, 9f, 10f, 4f);
    protected static final VoxelShape vs6_2 = Block.createCuboidShape(1f, 5f, 7f, 3f, 8f, 9f);
    protected static final VoxelShape vs7_2 = Block.createCuboidShape(3f, 7f, 7f, 4f, 10f, 9f);
    protected static final VoxelShape vs8_2 = Block.createCuboidShape(7f, 5f, 13f, 9f, 8f, 15f);
    protected static final VoxelShape vs9_2 = Block.createCuboidShape(7f, 7f, 12f, 9f, 10f, 13f);
    protected static final VoxelShape vs10_2 = Block.createCuboidShape(13f, 5f, 7f, 15f, 8f, 9f);
    protected static final VoxelShape vs11_2 = Block.createCuboidShape(12f, 7f, 7f, 13f, 10f, 9f);

    protected static final VoxelShape VOXEL_SHAPE_TOP = VoxelShapes.union(vs1_2, vs2_2, vs3_2, vs4_2, vs5_2, vs6_2, vs7_2, vs8_2, vs9_2, vs10_2, vs11_2).simplify();
    protected static final VoxelShape VOXEL_SHAPE_BOTTOM = VoxelShapes.union(vs1_1, vs2_1, vs3_1).simplify();

    public WaystoneBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(HALF, DoubleBlockHalf.LOWER).with(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new WaystoneBlockEntity();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(HALF, FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        if (blockPos.getY() < 255 && ctx.getWorld().getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
        } else {
            return null;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return VOXEL_SHAPE_BOTTOM;
        } else {
            return VOXEL_SHAPE_TOP;
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockPos newBlock;
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                newBlock = pos.down();
            } else {
                newBlock = pos.up();
            }
            BlockState newBlockState = world.getBlockState(newBlock);
            if (newBlockState.getBlock() == state.getBlock() && newBlockState.get(HALF) != state.get(HALF)) {
                world.setBlockState(newBlock, Blocks.AIR.getDefaultState(), 35);
                world.syncWorldEvent(player, 2001, newBlock, Block.getRawIdFromState(newBlockState));
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {

            if (player.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("empty_scroll")) {
                ItemStack waystoneScroll = new ItemStack(ItemRegistry.ITEMS.get("waystone_scroll"));
                List<Waystone> discovered = Waystones.WAYSTONE_DATABASE.getDiscoveredWaystones(player);
                if (discovered.size() == 0) {
                    return ActionResult.FAIL;
                }
                CompoundTag tag = new CompoundTag();
                for(Waystone waystone : discovered) {
                    tag.putString(waystone.name, waystone.name);
                }
                waystoneScroll.setTag(tag);
                player.getMainHandStack().decrement(1);
                player.inventory.offerOrDrop(world, waystoneScroll);
                return ActionResult.SUCCESS;
            }

            BlockPos openPos = pos;
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                openPos = pos.down();
            }
            WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) world.getBlockEntity(openPos);

            if (player.isSneaking() && player.hasPermissionLevel(2)) {
                for (ItemStack item : blockEntity.inventory) {
                    world.spawnEntity(new ItemEntity(world, openPos.getX(), openPos.getY() + 1.0, openPos.getZ(), item));
                }
                blockEntity.inventory.clear();
            }
            else {
                String worldName = world.getRegistryKey().getValue().getNamespace() + ":" + world.getRegistryKey().getValue().getPath();
                Waystone waystone = Waystones.WAYSTONE_DATABASE.getWaystone(openPos, worldName);
                String id;
                if (waystone != null) {
                    id = waystone.name;
                } else {
                    id = Utils.generateWaystoneName("");
                }

                if (!Waystones.WAYSTONE_DATABASE.containsWaystone(id)) {
                    Waystones.WAYSTONE_DATABASE.addWaystone(id, blockEntity);
                }

                if (!Waystones.WAYSTONE_DATABASE.playerHasDiscovered(player, id)) {
                    if (!Config.getInstance().canGlobalDiscover()) {
                        player.sendMessage(new LiteralText(id + " has been discovered!").formatted(Formatting.AQUA), false);
                    }
                    Waystones.WAYSTONE_DATABASE.discoverWaystone(player, id);
                }
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, openPos);

                if (screenHandlerFactory != null) {
                    Waystones.WAYSTONE_DATABASE.sendToPlayer((ServerPlayerEntity) player);
                    player.openHandledScreen(screenHandlerFactory);
                }
            }
        }
        world.getBlockEntity(pos).markDirty();
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
            Waystones.WAYSTONE_DATABASE.removeWaystone((WaystoneBlockEntity)world.getBlockEntity(pos.down()));
        } else {
            Waystones.WAYSTONE_DATABASE.removeWaystone((WaystoneBlockEntity)world.getBlockEntity(pos));
        }
    }

}
