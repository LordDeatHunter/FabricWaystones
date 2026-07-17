package wraith.fwaystones.block;

import net.minecraft.server.permissions.Permissions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.WaystoneScrollItem;
import wraith.fwaystones.registry.BlockEntityRegistry;
import wraith.fwaystones.util.Utils;
import java.util.Set;

public class WaystoneBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty GENERATED = BooleanProperty.create("generated");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty MOSSY = BooleanProperty.create("mossy");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final MapCodec<WaystoneBlock> CODEC = simpleCodec(WaystoneBlock::new);
    protected static final VoxelShape VOXEL_SHAPE_TOP;
    protected static final VoxelShape VOXEL_SHAPE_BOTTOM;

    static {
        // TOP
        VoxelShape vs1_1 = Block.box(1f, 0f, 1f, 15f, 2f, 15f);
        VoxelShape vs2_1 = Block.box(2f, 2f, 2f, 14f, 5f, 14f);
        VoxelShape vs3_1 = Block.box(3f, 5f, 3f, 13f, 16f, 13f);
        // BOTTOM
        VoxelShape vs1_2 = Block.box(3f, 0f, 3f, 13f, 1f, 13f);
        VoxelShape vs2_2 = Block.box(2f, 1f, 2f, 14f, 5f, 14f);
        VoxelShape vs3_2 = Block.box(3f, 5f, 3f, 13f, 7f, 13f);
        VoxelShape vs4_2 = Block.box(7f, 5f, 1f, 9f, 8f, 3f);
        VoxelShape vs5_2 = Block.box(7f, 7f, 3f, 9f, 10f, 4f);
        VoxelShape vs6_2 = Block.box(1f, 5f, 7f, 3f, 8f, 9f);
        VoxelShape vs7_2 = Block.box(3f, 7f, 7f, 4f, 10f, 9f);
        VoxelShape vs8_2 = Block.box(7f, 5f, 13f, 9f, 8f, 15f);
        VoxelShape vs9_2 = Block.box(7f, 7f, 12f, 9f, 10f, 13f);
        VoxelShape vs10_2 = Block.box(13f, 5f, 7f, 15f, 8f, 9f);
        VoxelShape vs11_2 = Block.box(12f, 7f, 7f, 13f, 10f, 9f);

        VOXEL_SHAPE_TOP = Shapes.or(vs1_2, vs2_2, vs3_2, vs4_2, vs5_2, vs6_2, vs7_2, vs8_2, vs9_2, vs10_2, vs11_2).optimize();
        VOXEL_SHAPE_BOTTOM = Shapes.or(vs1_1, vs2_1, vs3_1).optimize();
    }

    public WaystoneBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH).setValue(MOSSY, false).setValue(WATERLOGGED, false).setValue(ACTIVE, false).setValue(GENERATED, false));
    }

    @Nullable
    public static WaystoneBlockEntity getEntity(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof WaystoneBlock)) {
            return null;
        }
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }
        return world.getBlockEntity(pos) instanceof WaystoneBlockEntity waystone ? waystone : null;
    }

    public MapCodec<WaystoneBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? null : new WaystoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntity::ticker);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(HALF, FACING, MOSSY, WATERLOGGED, ACTIVE, GENERATED);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        var bottomState = world.getBlockState(pos);
        if (FabricWaystones.CONFIG.worldgen.unbreakable_generated_waystones() && state.getValue(GENERATED)) {
            return 0;
        }
        if (bottomState.getBlock() instanceof WaystoneBlock) {
            BlockPos entityPos = bottomState.getValue(WaystoneBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            switch (FabricWaystones.CONFIG.permission_level_for_breaking_waystones()) {
                case OWNER -> {
                    if (world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone && waystone.getOwner() != null && !player.getUUID().equals(waystone.getOwner())) {
                        return 0;
                    }
                }
                case OP -> {
                    if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                        return 0;
                    }
                }
                case NONE -> {
                    return 0;
                }
            }
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockPos = ctx.getClickedPos();

        var nbt = ctx.getItemInHand().get(DataComponents.CUSTOM_DATA);
        boolean hasOwner = nbt != null && nbt.copyTag().contains("waystone_owner");
        var world = ctx.getLevel();
        var fluidState = world.getFluidState(blockPos);

        if (blockPos.getY() < world.getMaxY() - 1 && world.getBlockState(blockPos.above()).canBeReplaced(ctx)) {
            return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(ACTIVE, hasOwner)
                .setValue(GENERATED, false);
        } else {
            return null;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? VOXEL_SHAPE_BOTTOM : VOXEL_SHAPE_TOP;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockPos topPos;
        BlockPos botPos;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            topPos = pos;
            botPos = pos.below();
        } else {
            topPos = pos.above();
            botPos = pos;
        }

        if (world.getBlockEntity(botPos) instanceof WaystoneBlockEntity waystone && !player.isCreative() && player.hasCorrectToolForDrops(world.getBlockState(botPos)) && world instanceof ServerLevel) {
            if (!world.isClientSide()) {
                ItemStack itemStack = new ItemStack(state.getBlock().asItem());
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
                waystone.saveAdditional(output);
                var compoundTag = output.buildResult();
                if (FabricWaystones.CONFIG.store_waystone_data_on_sneak_break() && player.isShiftKeyDown() && !compoundTag.isEmpty()) {
                    itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
                }
                Containers.dropItemStack(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, itemStack);
                if (waystone.getBlockState().getValue(MOSSY)) {
                    Containers.dropItemStack(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, new ItemStack(Items.VINE));
                }
            } else {
                waystone.unpackLootTable(player);
            }

            FabricWaystones.WAYSTONE_STORAGE.removeWaystone(waystone);
        }

        world.removeBlock(topPos, false);
        world.removeBlock(botPos, false);
        world.updateNeighborsAt(topPos, Blocks.AIR);

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            super.setPlacedBy(world, pos, state, placer, itemStack);
            return;
        }
        var fluidState = world.getFluidState(pos.above());
        world.setBlockAndUpdate(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER));
        BlockEntity entity = world.getBlockEntity(pos);
        if (placer instanceof ServerPlayer && entity instanceof WaystoneBlockEntity waystone) {
            FabricWaystones.WAYSTONE_STORAGE.tryAddWaystoneFromItemstack(waystone, itemStack);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    //    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos openPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState topState = world.getBlockState(openPos.above());
        BlockState bottomState = world.getBlockState(openPos);
        InteractionHand hand = player.getUsedItemHand();
        Item heldItem = player.getItemInHand(hand).getItem();
        if (heldItem == Items.VINE) {
            if (!topState.getValue(MOSSY)) {
                world.setBlockAndUpdate(openPos.above(), topState.setValue(MOSSY, true));
                world.setBlockAndUpdate(openPos, bottomState.setValue(MOSSY, true));
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
            }
            return InteractionResult.PASS;
        }

        if (heldItem == Items.SHEARS) {
            if (topState.getValue(MOSSY)) {
                world.setBlockAndUpdate(openPos.above(), topState.setValue(MOSSY, false));
                world.setBlockAndUpdate(openPos, bottomState.setValue(MOSSY, false));
                var dropPos = openPos.above(2);
                Containers.dropItemStack(world, dropPos.getX() + 0.5F, dropPos.getY() + 0.5F, dropPos.getZ() + 0.5F, new ItemStack(Items.VINE));
            }
            return InteractionResult.PASS;
        }
        if (heldItem instanceof WaystoneScrollItem || heldItem instanceof LocalVoidItem || heldItem instanceof WaystoneDebuggerItem) {
            return InteractionResult.PASS;
        }

        WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) world.getBlockEntity(openPos);
        if (blockEntity == null) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown() && (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || (FabricWaystones.CONFIG.can_owners_redeem_payments() && player.getUUID().equals(blockEntity.getOwner())))) {
            if (blockEntity.hasStorage()) {
                Containers.dropContents(world, openPos.above(2), blockEntity.getInventory());
                blockEntity.setInventory(NonNullList.withSize(0, ItemStack.EMPTY));
            }
            return InteractionResult.SUCCESS;
        }
//        if (!FabricWaystones.CONFIG.discover_waystone_on_map_use() && FabricLoader.getInstance().isModLoaded("pinlib") && PinlibPlugin.tryUseOnMarkableBlock(player.getStackInHand(hand), world, openPos))
//            return ActionResult.SUCCESS;

        FabricWaystones.WAYSTONE_STORAGE.tryAddWaystone(blockEntity);
        PlayerEntityMixinAccess playerAccess = (PlayerEntityMixinAccess) player;
        Set<String> discovered = playerAccess.fabricWaystones$getDiscoveredWaystones();
        if (!discovered.contains(blockEntity.getHash())) {
            if (!blockEntity.isGlobal()) {
                Identifier discoverItemId = Utils.getDiscoverItem();
                if (!player.isCreative()) {
                    Item discoverItem = BuiltInRegistries.ITEM.getValue(discoverItemId);
                    int discoverAmount = FabricWaystones.CONFIG.take_amount_from_discover_item();
                    if (!Utils.containsItem(player.getInventory(), discoverItem, discoverAmount)) {
                        player.sendSystemMessage(Component.translatable(
                            "fwaystones.missing_discover_item",
                            discoverAmount,
                            Component.translatable(discoverItem.getDescriptionId()).withStyle(style ->
                                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.missing_discover_item.arg_color").getString()).getOrThrow())
                            )
                        ));
                        return InteractionResult.FAIL;
                    } else if (discoverItem != Items.AIR) {
                        Utils.removeItem(player.getInventory(), discoverItem, discoverAmount);
                        player.sendSystemMessage(Component.translatable(
                            "fwaystones.discover_item_paid",
                            discoverAmount,
                            Component.translatable(discoverItem.getDescriptionId()).withStyle(style ->
                                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.discover_item_paid.arg_color").getString()).getOrThrow())
                            )
                        ));
                    }
                }
                player.sendSystemMessage(Component.translatable(
                    "fwaystones.discover_waystone",
                    Component.literal(blockEntity.getWaystoneName()).withStyle(style ->
                        style.withColor(TextColor.parseColor(Component.translatable("fwaystones.discover_waystone.arg_color").getString()).getOrThrow())
                    )
                ));
            }
            playerAccess.fabricWaystones$discoverWaystone(blockEntity);
        }
        if (blockEntity.getOwner() == null) {
            blockEntity.setOwner(player);
        } else {
            blockEntity.updateActiveState();
        }

        MenuProvider screenHandlerFactory = state.getMenuProvider(world, pos);

        if (screenHandlerFactory != null) {
            player.openMenu(screenHandlerFactory);
        }

        blockEntity.setChanged();
        return InteractionResult.SUCCESS_SERVER;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return super.getMenuProvider(state, world, state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockPos newPos;
        DoubleBlockHalf verticalPosition;

        if (state.getBlock() != this) {
            super.affectNeighborsAfterRemoval(state, world, pos, moved);
            return;
        }

        BlockState newState = world.getBlockState(pos);

        if (state.getValue(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
            newPos = pos.below();
            verticalPosition = DoubleBlockHalf.LOWER;
        } else {
            newPos = pos.above();
            verticalPosition = DoubleBlockHalf.UPPER;
        }

        if (!(newState.getBlock() instanceof WaystoneBlock)) {
            BlockPos testPos = pos;
            if (state.getValue(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
                testPos = pos.below();
            }
            BlockEntity entity = world.getBlockEntity(testPos);
            if (entity instanceof WaystoneBlockEntity waystone) {
                FabricWaystones.WAYSTONE_STORAGE.removeWaystone(waystone);
            }
            world.removeBlockEntity(testPos);
            world.setBlockAndUpdate(newPos, newState);
        } else {
            var fluid = world.getFluidState(newPos).getType() == Fluids.WATER && verticalPosition == DoubleBlockHalf.LOWER;
            world.setBlockAndUpdate(newPos, newState.setValue(WaystoneBlock.HALF, verticalPosition).setValue(WATERLOGGED, fluid));
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public BlockState updateShape(
            BlockState state,
            LevelReader world,
            ScheduledTickAccess tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

}
