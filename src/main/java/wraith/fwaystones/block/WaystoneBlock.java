package wraith.fwaystones.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.WaystoneType;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.api.WaystoneDataStorage;

import static wraith.fwaystones.FabricWaystones.*;

@SuppressWarnings("deprecation")
public class WaystoneBlock extends BlockWithEntity implements Waterloggable {

    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty GENERATED = BooleanProperty.of("generated");
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty MOSSY = BooleanProperty.of("mossy");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final MapCodec<WaystoneBlock> CODEC = createCodec(WaystoneBlock::new);
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

        VOXEL_SHAPE_TOP = VoxelShapes.union(vs1_2, vs2_2, vs3_2, vs4_2, vs5_2, vs6_2, vs7_2, vs8_2, vs9_2, vs10_2, vs11_2);
        VOXEL_SHAPE_BOTTOM = VoxelShapes.union(vs1_1, vs2_1, vs3_1);
    }

    public final WaystoneType type;

    public WaystoneBlock(AbstractBlock.Settings settings) {
        //TODO: fix this
        this(null, settings);
    }

    public WaystoneBlock(WaystoneType type, AbstractBlock.Settings settings) {
        super(settings);

        this.type = type;

        setDefaultState(
                getStateManager().getDefaultState()
                        .with(HALF, DoubleBlockHalf.LOWER)
                        .with(FACING, Direction.NORTH)
                        .with(MOSSY, false)
                        .with(WATERLOGGED, false)
                        .with(ACTIVE, false)
                        .with(GENERATED, false)
        );

        WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY.addSupportedBlock(this);
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

    public MapCodec<WaystoneBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? null : new WaystoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY, (world1, pos, state1, be) -> {
            be.tick();
        });
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(HALF, FACING, MOSSY, WATERLOGGED, ACTIVE, GENERATED);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var bottomState = world.getBlockState(pos);
        var config = FabricWaystones.CONFIG;

        if (config.unbreakableGeneratedWaystones() && state.get(GENERATED)) {
            return 0;
        }

        if (bottomState.getBlock() instanceof WaystoneBlock) {
            BlockPos entityPos = bottomState.get(WaystoneBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
            if (world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone){
                var owner = waystone.getData().owner();
                switch (config.breakingWaystonePermission()) {
                    case OWNER -> {
                        if (owner != null && !player.getUuid().equals(owner)) return 0;
                    }
                    case OP -> {
                        if (!player.hasPermissionLevel(2)) return 0;
                    }
                    case NONE -> {
                        return 0;
                    }
                }
            }

        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();

        var nbt = ctx.getStack().get(DataComponentTypes.BLOCK_ENTITY_DATA);
        boolean hasOwner = nbt != null && nbt.contains("waystone_owner");
        var world = ctx.getWorld();
        var fluidState = world.getFluidState(blockPos);

        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
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

    public static final Identifier WAYSTONE_BLOCK_DROP = Identifier.ofVanilla("waystone_block_drop");

//    @Override
//    protected List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
//        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
//        if (blockEntity instanceof WaystoneBlockEntity waystoneBlockEntity) {
//            builder = builder.addDynamicDrop(WAYSTONE_BLOCK_DROP, lootConsumer -> {
//                for (int i = 0; i < waystoneBlockEntity.size(); i++) {
//                    lootConsumer.accept(waystoneBlockEntity.getStack(i));
//                }
//            });
//        }
//        return super.getDroppedStacks(state, builder);
//    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos topPos;
        BlockPos botPos;

        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            topPos = pos;
            botPos = pos.down();
        } else {
            topPos = pos.up();
            botPos = pos;
        }

        if (world.getBlockEntity(botPos) instanceof WaystoneBlockEntity waystone) {
            if (!world.isClient) {
                var itemStack = new ItemStack(state.getBlock().asItem());

                var hasData = waystone.hasData();

                var shouldDrop = false;

                if (hasData) {
                    waystone.setStackNbt(itemStack, world.getRegistryManager());

                    shouldDrop = true;
                }

                if ((shouldDrop && player.isCreative()) || !player.isCreative()) {
                    ItemScatterer.spawn(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, itemStack);

                    if (waystone.getCachedState().get(MOSSY)) {
                        ItemScatterer.spawn(world, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, new ItemStack(Items.VINE));
                    }
                }

                if (hasData) {
                    var uuid = waystone.getUUID();

                    if (uuid != null) {
                        WaystoneDataStorage.getStorage(world).removePosition(uuid);
                    }
                }
            } else {
                waystone.generateLoot(player);
            }
        }

        world.removeBlock(topPos, false);
        world.removeBlock(botPos, false);
        world.updateNeighbors(topPos, Blocks.AIR);

        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            super.onPlaced(world, pos, state, placer, itemStack);
            return;
        }
        var fluidState = world.getFluidState(pos.up());
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER));

        if (placer instanceof ServerPlayerEntity && world.getBlockEntity(pos) instanceof WaystoneBlockEntity blockEntity) {
            blockEntity.updateActiveState();
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var openPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        var topState = world.getBlockState(openPos.up());
        var bottomState = world.getBlockState(openPos);

        var hand = player.getActiveHand();
        var stack = player.getStackInHand(hand);
        var item = stack.getItem();

        if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) return ActionResult.PASS;
        if (stack.isIn(FabricWaystones.LOCAL_VOID_ITEM)) return ActionResult.PASS;
        if (item instanceof WaystoneDebuggerItem) return ActionResult.PASS;

        if (stack.isIn(WAYSTONE_MOSS_APPLIERS)) {
            if (!topState.get(MOSSY)) {
                if (!world.isClient) {
                    ItemOps.decrementPlayerHandItem(player, hand);
                    world.setBlockState(openPos.up(), topState.with(MOSSY, true));
                    world.setBlockState(openPos, bottomState.with(MOSSY, true));
                    world.playSound(null, pos, WAYSTONE_MOSS_APPLY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        if (stack.isIn(ConventionalItemTags.SHEAR_TOOLS)) {
            if (topState.get(MOSSY)) {
                if (!world.isClient) {
                    world.setBlockState(openPos.up(), topState.with(MOSSY, false));
                    world.setBlockState(openPos, bottomState.with(MOSSY, false));
                    var dropPos = openPos.up(2);
                    ItemScatterer.spawn(world, dropPos.getX() + 0.5F, dropPos.getY() + 0.5F, dropPos.getZ() + 0.5F, new ItemStack(Items.VINE));
                    world.playSound(null, pos, WAYSTONE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) world.getBlockEntity(openPos);
        if (blockEntity == null) return ActionResult.FAIL;

        var storage = WaystoneDataStorage.getStorage(player);
        var data = world.isClient ? storage.getData(blockEntity.getUUID()) : storage.createGetOrImportData(blockEntity, this.type.defaultColor());
        if (data == null) return ActionResult.PASS;

        if (item instanceof DyeItem dyeItem) {
            var color = dyeItem.getColor().getSignColor();
            if (data.color() != color) {
                ItemOps.decrementPlayerHandItem(player, hand);
                storage.recolorWaystone(data.uuid(), color);
                blockEntity.markDirty();
                world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        }

        if (stack.isIn(WAYSTONE_CLEANERS)) {
            if (data.color() != this.type.defaultColor()) {
                storage.recolorWaystone(data.uuid(), this.type.defaultColor());
                blockEntity.markDirty();
                if (stack.isIn(WAYSTONE_BUCKET_CLEANERS)) {
                    if (world.getRandom().nextInt(100) == 69) {
                        world.playSound(null, pos, WAYSTONE_CLEAN_BUCKET_STEAL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        player.sendEquipmentBreakStatus(item, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        stack.decrement(1);
                    } else {
                        world.playSound(null, pos, WAYSTONE_CLEAN_BUCKET, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    world.playSound(null, pos, WAYSTONE_CLEAN_SPONGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                return ActionResult.SUCCESS;
            }
        }

        if (world.isClient) return ActionResult.SUCCESS;

        if (player.isSneaking() && (player.hasPermissionLevel(2) || (FabricWaystones.CONFIG.allowOwnersToRedeemPayments() && player.getUuid().equals(data.owner())))) {
            if (blockEntity.hasStorage()) {
                ItemScatterer.spawn(world, openPos.up(2), blockEntity.getInventory());
                blockEntity.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
                return ActionResult.SUCCESS;
            }
        }

        var discovered = WaystonePlayerData.getData(player).discoveredWaystones();
        if (!discovered.contains(blockEntity.getUUID())) {
            if (!data.global()) {
                Identifier discoverItemId = Utils.getDiscoverItem();
                if (!player.isCreative()) {
                    Item discoverItem = Registries.ITEM.get(discoverItemId);
                    int discoverAmount = FabricWaystones.CONFIG.requiredDiscoveryAmount();
                    if (!Utils.containsItem(player.getInventory(), discoverItem, discoverAmount)) {
                        player.sendMessage(
                                TextUtils.translationWithArg(
                                        "missing_discover_item",
                                        discoverAmount,
                                        Text.translatable(discoverItem.getTranslationKey())
                                ), false);
                        return ActionResult.FAIL;
                    } else if (discoverItem != Items.AIR) {
                        Utils.removeItem(player.getInventory(), discoverItem, discoverAmount);
                        player.sendMessage(TextUtils.translationWithArg(
                            "discover_item_paid",
                            discoverAmount,
                            Text.translatable(discoverItem.getTranslationKey())
                        ), false);
                    }
                }

                player.sendMessage(TextUtils.translationWithArg(
                    "discover_waystone",
                        data.name()
                ), false);
            }
            WaystonePlayerData.getData(player).discoverWaystone(blockEntity.getUUID());
        }

        if (data.owner() == null) {
            blockEntity.setOwner(player);
        } else {
            blockEntity.updateActiveState();
        }

        NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

        if (screenHandlerFactory != null) player.openHandledScreen(screenHandlerFactory);

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
                var uuid = waystone.getUUID();
                if (uuid != null) {
                    WaystoneDataStorage.getStorage(world).removePosition(uuid);
                }
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
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

}
