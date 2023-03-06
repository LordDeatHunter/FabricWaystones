package wraith.fwaystones.block;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.integration.pinlib.PinlibPlugin;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.WaystoneScrollItem;
import wraith.fwaystones.registry.BlockEntityRegistry;
import wraith.fwaystones.util.Utils;

@SuppressWarnings("deprecation")
public class WaystoneBlock extends BlockWithEntity implements Waterloggable {

    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty GENERATED = BooleanProperty.of("generated");
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty MOSSY = BooleanProperty.of("mossy");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected static final VoxelShape VOXEL_SHAPE_TOP;
    protected static final VoxelShape VOXEL_SHAPE_BOTTOM;

    static {
        // TOP
        VoxelShape vs1_1 = Block.createCuboidShape(1f, 0f, 1f, 15f, 2f, 15f);
        VoxelShape vs2_1 = Block.createCuboidShape(2f, 2f, 2f, 14f, 5f, 14f);
        VoxelShape vs3_1 = Block.createCuboidShape(3f, 5f, 3f, 13f, 16f, 13f);
        // BOTTOM
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

    public WaystoneBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(HALF, DoubleBlockHalf.LOWER).with(FACING, Direction.NORTH).with(MOSSY, false).with(WATERLOGGED, false).with(ACTIVE, false).with(GENERATED, false));
    }

    @Nullable
    public static WaystoneBlockEntity getEntity(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof WaystoneBlock)) {
            return null;
        }
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.down();
        }
        return world.getBlockEntity(pos) instanceof WaystoneBlockEntity waystone ? waystone : null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? null : new WaystoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntity::ticker);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(HALF, FACING, MOSSY, WATERLOGGED, ACTIVE, GENERATED);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var bottomState = world.getBlockState(pos);
        if (FabricWaystones.CONFIG.worldgen.unbreakable_generated_waystones() && state.get(GENERATED)) {
            return 0;
        }
        if (bottomState.getBlock() instanceof WaystoneBlock) {
            BlockPos entityPos = bottomState.get(WaystoneBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
            switch (FabricWaystones.CONFIG.permission_level_for_breaking_waystones()) {
                case OWNER -> {
                    if (world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone && waystone.getOwner() != null && !player.getUuid().equals(waystone.getOwner())) {
                        return 0;
                    }
                }
                case OP -> {
                    if (!player.hasPermissionLevel(2)) {
                        return 0;
                    }
                }
                case NONE -> {
                    return 0;
                }
            }
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();

        var nbt = ctx.getStack().getSubNbt("BlockEntityTag");
        boolean hasOwner = nbt != null && nbt.contains("waystone_owner");
        var world = ctx.getWorld();
        var fluidState = world.getFluidState(blockPos);

        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                .with(FACING, ctx.getPlayerFacing().getOpposite())
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(ACTIVE, hasOwner)
                .with(GENERATED, false);
        } else {
            return null;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? VOXEL_SHAPE_BOTTOM : VOXEL_SHAPE_TOP;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos topPos;
        BlockPos botPos;
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            topPos = pos;
            botPos = pos.down();
        } else {
            topPos = pos.up();
            botPos = pos;
        }

        if (world.getBlockEntity(botPos) instanceof WaystoneBlockEntity waystone && !player.isCreative() && player.canHarvest(world.getBlockState(botPos)) && world instanceof ServerWorld) {
            if (!world.isClient) {
                ItemStack itemStack = new ItemStack(state.getBlock().asItem());
                var compoundTag = new NbtCompound();
                waystone.writeNbt(compoundTag);
                if (FabricWaystones.CONFIG.store_waystone_data_on_sneak_break() && player.isSneaking() && !compoundTag.isEmpty()) {
                    itemStack.setSubNbt("BlockEntityTag", compoundTag);
                }
                ItemScatterer.spawn(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, itemStack);
                if (waystone.getCachedState().get(MOSSY)) {
                    ItemScatterer.spawn(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, new ItemStack(Items.VINE));
                }
            } else {
                waystone.checkLootInteraction(player);
            }
        }

        world.removeBlock(topPos, false);
        world.removeBlock(botPos, false);
        world.updateNeighbors(topPos, Blocks.AIR);

        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            super.onPlaced(world, pos, state, placer, itemStack);
            return;
        }
        var fluidState = world.getFluidState(pos.up());
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER));
        BlockEntity entity = world.getBlockEntity(pos);
        if (placer instanceof ServerPlayerEntity && entity instanceof WaystoneBlockEntity waystone) {
            FabricWaystones.WAYSTONE_STORAGE.tryAddWaystone(waystone);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.success(true);
        }
        BlockPos openPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        BlockState topState = world.getBlockState(openPos.up());
        BlockState bottomState = world.getBlockState(openPos);
        Item heldItem = player.getStackInHand(hand).getItem();
        if (heldItem == Items.VINE) {
            if (!topState.get(MOSSY)) {
                world.setBlockState(openPos.up(), topState.with(MOSSY, true));
                world.setBlockState(openPos, bottomState.with(MOSSY, true));
                if (!player.isCreative()) {
                    player.getStackInHand(hand).decrement(1);
                }
            }
            return ActionResult.PASS;
        }
        if (heldItem == Items.SHEARS) {
            if (topState.get(MOSSY)) {
                world.setBlockState(openPos.up(), topState.with(MOSSY, false));
                world.setBlockState(openPos, bottomState.with(MOSSY, false));
                var dropPos = openPos.up(2);
                ItemScatterer.spawn(world, dropPos.getX() + 0.5F, dropPos.getY() + 0.5F, dropPos.getZ() + 0.5F, new ItemStack(Items.VINE));
            }
            return ActionResult.PASS;
        }
        if (heldItem instanceof WaystoneScrollItem || heldItem instanceof LocalVoidItem || heldItem instanceof WaystoneDebuggerItem) {
            return ActionResult.PASS;
        }

        var discovered = ((PlayerEntityMixinAccess) player).getDiscoveredWaystones();

        WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) world.getBlockEntity(openPos);
        if (blockEntity == null) {
            return ActionResult.FAIL;
        }

        if (player.isSneaking() && (player.hasPermissionLevel(2) || (FabricWaystones.CONFIG.can_owners_redeem_payments() && player.getUuid().equals(blockEntity.getOwner())))) {
            if (blockEntity.hasStorage()) {
                ItemScatterer.spawn(world, openPos.up(2), blockEntity.getInventory());
                blockEntity.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
            }
        } else {
            if (!FabricWaystones.CONFIG.discover_waystone_on_map_use() && FabricLoader.getInstance().isModLoaded("pinlib") && PinlibPlugin.tryUseOnMarkableBlock(player.getStackInHand(hand), world, openPos))
                return ActionResult.SUCCESS;

            FabricWaystones.WAYSTONE_STORAGE.tryAddWaystone(blockEntity);
            if (!discovered.contains(blockEntity.getHash())) {
                if (!blockEntity.isGlobal()) {
                    var discoverItemId = Utils.getDiscoverItem();
                    if (!player.isCreative()) {
                        var discoverItem = Registry.ITEM.get(discoverItemId);
                        var discoverAmount = FabricWaystones.CONFIG.take_amount_from_discover_item();
                        if (!Utils.containsItem(player.getInventory(), discoverItem, discoverAmount)) {
                            player.sendMessage(Text.translatable(
                                "fwaystones.missing_discover_item",
                                discoverAmount,
                                Text.translatable(discoverItem.getTranslationKey()).styled(style ->
                                    style.withColor(TextColor.parse(Text.translatable("fwaystones.missing_discover_item.arg_color").getString()))
                                )
                            ), false);
                            return ActionResult.FAIL;
                        } else if (discoverItem != Items.AIR) {
                            Utils.removeItem(player.getInventory(), discoverItem, discoverAmount);
                            player.sendMessage(Text.translatable(
                                "fwaystones.discover_item_paid",
                                discoverAmount,
                                Text.translatable(discoverItem.getTranslationKey()).styled(style ->
                                    style.withColor(TextColor.parse(Text.translatable("fwaystones.discover_item_paid.arg_color").getString()))
                                )
                            ), false);
                        }
                    }
                    player.sendMessage(Text.translatable(
                        "fwaystones.discover_waystone",
                        Text.literal(blockEntity.getWaystoneName()).styled(style ->
                            style.withColor(TextColor.parse(Text.translatable("fwaystones.discover_waystone.arg_color").getString()))
                        )
                    ), false);
                }
                ((PlayerEntityMixinAccess) player).discoverWaystone(blockEntity);
            }
            if (blockEntity.getOwner() == null) {
                blockEntity.setOwner(player);
            } else {
                blockEntity.updateActiveState();
            }

            var screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null)
                player.openHandledScreen(screenHandlerFactory);
        }
        blockEntity.markDirty();
        return ActionResult.success(false);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return super.createScreenHandlerFactory(state, world, state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockPos newPos;
        DoubleBlockHalf verticalPosition;

        if (state.getBlock() != this) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
            newPos = pos.down();
            verticalPosition = DoubleBlockHalf.LOWER;
        } else {
            newPos = pos.up();
            verticalPosition = DoubleBlockHalf.UPPER;
        }

        if (!(newState.getBlock() instanceof WaystoneBlock)) {
            BlockPos testPos = pos;
            if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
                testPos = pos.down();
            }
            BlockEntity entity = world.getBlockEntity(testPos);
            if (!world.isClient && entity instanceof WaystoneBlockEntity waystone) {
                FabricWaystones.WAYSTONE_STORAGE.removeWaystone(waystone);
            }
            world.removeBlockEntity(testPos);
            world.setBlockState(newPos, newState);
        } else {
            var fluid = world.getFluidState(newPos).getFluid() == Fluids.WATER && verticalPosition == DoubleBlockHalf.LOWER;
            world.setBlockState(newPos, newState.with(WaystoneBlock.HALF, verticalPosition).with(WATERLOGGED, fluid));
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

}
