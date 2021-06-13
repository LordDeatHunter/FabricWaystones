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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import wraith.waystones.Config;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Waystones;
import wraith.waystones.item.LocalVoid;
import wraith.waystones.item.WaystoneScroll;
import wraith.waystones.registries.BlockRegistry;
import wraith.waystones.registries.ItemRegistry;

import java.util.HashSet;

public class WaystoneBlock extends BlockWithEntity {

    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    protected static final VoxelShape VOXEL_SHAPE_TOP;
    protected static final VoxelShape VOXEL_SHAPE_BOTTOM;

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
        BlockPos topPos;
        BlockPos botPos;
        BlockEntity entity;
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            topPos = pos;
            botPos = pos.down();
        } else {
            topPos = pos.up();
            botPos = pos;
        }

        entity = world.getBlockEntity(botPos);
        if (entity instanceof WaystoneBlockEntity) {
            /*
            dropStacks(state, world, botPos);
            */
            if (!player.isCreative() && player.isUsingEffectiveTool(world.getBlockState(botPos)) && world instanceof ServerWorld) {
                WaystoneBlockEntity waystoneBlockEntity = (WaystoneBlockEntity)entity;
                if (!world.isClient) {
                    ItemStack itemStack = new ItemStack(ItemRegistry.ITEMS.get("waystone"));
                    CompoundTag compoundTag = waystoneBlockEntity.toTag(new CompoundTag());
                    if (!compoundTag.isEmpty()) {
                        itemStack.putSubTag("BlockEntityTag", compoundTag);
                    }
                    ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemStack);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);
                } else {
                    waystoneBlockEntity.checkLootInteraction(player);
                }
            }
            if (Waystones.WAYSTONE_STORAGE != null) {
                Waystones.WAYSTONE_STORAGE.removeWaystone((WaystoneBlockEntity) entity);
            }
            world.removeBlockEntity(botPos);
        }

        entity = world.getBlockEntity(topPos);
        if (Waystones.WAYSTONE_STORAGE != null && entity instanceof WaystoneBlockEntity) {
            Waystones.WAYSTONE_STORAGE.removeWaystone((WaystoneBlockEntity) entity);
            world.removeBlockEntity(topPos);
        }
        world.removeBlock(topPos, false);
        world.updateNeighbors(topPos, Blocks.AIR);

        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER));
        BlockEntity entity = world.getBlockEntity(pos);
        if (placer instanceof ServerPlayerEntity && entity instanceof WaystoneBlockEntity) {
            ((WaystoneBlockEntity) entity).setOwner((PlayerEntity)placer);
            if (Waystones.WAYSTONE_STORAGE != null) {
                Waystones.WAYSTONE_STORAGE.addWaystone(((WaystoneBlockEntity) entity));
            }
        }
    }

    public static WaystoneBlockEntity getEntity(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != BlockRegistry.WAYSTONE) {
            return null;
        }
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.down();
        }
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof WaystoneBlockEntity) {
            return (WaystoneBlockEntity) entity;
        } else {
            return null;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {

            HashSet<String> discovered = ((PlayerEntityMixinAccess) player).getDiscoveredWaystones();

            if (player.getMainHandStack().getItem() instanceof WaystoneScroll || player.getMainHandStack().getItem() instanceof LocalVoid) {
                return ActionResult.PASS;
            }

            BlockPos openPos = pos;
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                openPos = pos.down();
            }
            WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) world.getBlockEntity(openPos);
            if (blockEntity == null) {
                return ActionResult.FAIL;
            }

            if ((player.isSneaking() && (player.hasPermissionLevel(2)) || (Config.getInstance().canOwnersRedeemPayments() && blockEntity.getOwner().equals(player.getUuid())))) {
                if (blockEntity.hasStorage()) {
                    ItemScatterer.spawn(world, openPos.up(2), blockEntity.getInventory());
                    blockEntity.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
                }
            } else {
                if (blockEntity.getOwner() == null) {
                    blockEntity.setOwner(player);
                }

                if (!Waystones.WAYSTONE_STORAGE.containsHash(blockEntity.getHash())) {
                    Waystones.WAYSTONE_STORAGE.addWaystone(blockEntity);
                }

                if (!discovered.contains(blockEntity.getHash())) {
                    if (!blockEntity.isGlobal()) {
                        player.sendMessage(new LiteralText(blockEntity.getWaystoneName() + " ").append(new TranslatableText("waystones.discover_waystone")).formatted(Formatting.AQUA), false);
                    }
                    ((PlayerEntityMixinAccess) player).discoverWaystone(blockEntity);
                }

                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, openPos);

                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            }
            blockEntity.markDirty();
        }
        return ActionResult.success(world.isClient());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockPos newPos;
        DoubleBlockHalf facing;
        world.removeBlockEntity(pos);
        if (state.getBlock() != this) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
            newPos = pos.down();
            facing = DoubleBlockHalf.LOWER;
        } else {
            newPos = pos.up();
            facing = DoubleBlockHalf.UPPER;
        }

        if (newState.isAir()) {
            world.setBlockState(newPos, newState);
            world.removeBlockEntity(newPos);
        }
        if (newState.getBlock() != BlockRegistry.WAYSTONE) {
            BlockPos testPos = pos;
            if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
                testPos = pos.down();
            }
            BlockEntity entity = world.getBlockEntity(testPos);
            if (entity instanceof WaystoneBlockEntity) {
                Waystones.WAYSTONE_STORAGE.removeWaystone(((WaystoneBlockEntity) entity).getHash());
            }
        } else {
            world.setBlockState(newPos, newState.with(WaystoneBlock.HALF, facing));
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }



    public static String getDimensionName(World world) {
        return world.getRegistryKey().getValue().toString();
    }

    static {
        //TOP
        VoxelShape vs1_1 = Block.createCuboidShape(1f, 0f, 1f, 15f, 2f, 15f);
        VoxelShape vs2_1 = Block.createCuboidShape(2f, 2f, 2f, 14f, 5f, 14f);
        VoxelShape vs3_1 = Block.createCuboidShape(3f, 5f, 3f, 13f, 16f, 13f);
        //BOTTOM
        VoxelShape vs1_2 = Block.createCuboidShape(3f, 0f, 3f, 13f, 1f, 13f);
        VoxelShape vs2_2 = Block.createCuboidShape(2f, 1f, 2f, 14f, 5f, 14f);
        VoxelShape vs3_2 = Block.createCuboidShape(3f, 5f, 3f, 13f, 7f, 13f);
        VoxelShape vs4_2 = Block.createCuboidShape(7f, 5f, 1f, 9f, 8f, 3f);
        VoxelShape vs5_2 = Block.createCuboidShape(7f, 7f, 3f, 9f, 10f, 4f);
        VoxelShape vs6_2 = Block.createCuboidShape(1f, 5f, 7f, 3f, 8f, 9f);
        VoxelShape vs7_2 = Block.createCuboidShape(3f, 7f, 7f, 4f, 10f, 9f);
        VoxelShape vs8_2 = Block.createCuboidShape(7f, 5f, 13f, 9f, 8f, 15f);
        VoxelShape vs9_2 = Block.createCuboidShape(7f, 7f, 12f, 9f, 10f, 13f);
        VoxelShape vs10_2 = Block.createCuboidShape(13f, 5f, 7f, 15f, 8f, 9f);
        VoxelShape vs11_2 = Block.createCuboidShape(12f, 7f, 7f, 13f, 10f, 9f);

        VOXEL_SHAPE_TOP = VoxelShapes.union(vs1_2, vs2_2, vs3_2, vs4_2, vs5_2, vs6_2, vs7_2, vs8_2, vs9_2, vs10_2, vs11_2).simplify();
        VOXEL_SHAPE_BOTTOM = VoxelShapes.union(vs1_1, vs2_1, vs3_1).simplify();
    }

}
