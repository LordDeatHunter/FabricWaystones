package wraith.fwaystones.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.endec.Endec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.item.components.WaystoneHashTarget;
import wraith.fwaystones.mixin.TallBlantBlockAccessor;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.registry.WaystoneBlocks;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.registry.WaystoneItems;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.ArrayList;
import java.util.List;

import static wraith.fwaystones.FabricWaystones.*;

@SuppressWarnings("deprecation")
public class WaystoneBlock extends BlockWithEntity implements Waterloggable {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty GENERATED = BooleanProperty.of("generated");
    private static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final MapCodec<WaystoneBlock> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
                createSettingsCodec(),
                CodecUtils.toCodec(Endec.forEnum(WaystoneShape.class)).fieldOf("shape").forGetter(WaystoneBlock::shape)
            )
            .apply(instance, WaystoneBlock::of)
    );

    protected static final VoxelShape OUTLINE_SHAPE_BOTTOM = VoxelShapes.union(
        Block.createCuboidShape(1f, 0f, 1f, 15f, 2f, 15f),
        Block.createCuboidShape(2f, 2f, 2f, 14f, 5f, 14f),
        Block.createCuboidShape(3f, 5f, 3f, 13f, 16f, 13f)
    );
    protected static final VoxelShape OUTLINE_SHAPE_TOP = createTopShape(true, false);
    protected static final VoxelShape OUTLINE_SHAPE_SINGLE = createTopShape(true, true);

    protected static final VoxelShape COLLISION_SHAPE_BOTTOM = OUTLINE_SHAPE_BOTTOM;
    protected static final VoxelShape COLLISION_SHAPE_TOP = createTopShape(false, false);
    protected static final VoxelShape COLLISION_SHAPE_SINGLE = createTopShape(false, true);

    private static VoxelShape createTopShape(boolean addSpikes, boolean singleBlock) {
        var shapes = new ArrayList<VoxelShape>();

        if (singleBlock) {
            shapes.add(Block.createCuboidShape(1f, 0f, 1f, 15f, 1f, 15f));
            shapes.add(Block.createCuboidShape(2f, 1f, 2f, 14f, 2f, 14f));
            shapes.add(Block.createCuboidShape(3f, 2f, 3f, 13f, 4f, 13f));
            shapes.add(Block.createCuboidShape(2f, 4f, 2f, 14f, 8f, 14f));
            shapes.add(Block.createCuboidShape(3f, 8f, 3f, 13f, 10f, 13f));
        } else {
            shapes.add(Block.createCuboidShape(3f, 0f, 3f, 13f, 1f, 13f));
            shapes.add(Block.createCuboidShape(2f, 1f, 2f, 14f, 5f, 14f));
            shapes.add(Block.createCuboidShape(3f, 5f, 3f, 13f, 7f, 13f));
        }

        if (addSpikes) {
            List<VoxelShape> spikes = new ArrayList<>();

            spikes.add(Block.createCuboidShape(7f, 5f, 1f, 9f, 8f, 3f));
            spikes.add(Block.createCuboidShape(7f, 7f, 3f, 9f, 10f, 4f));
            spikes.add(Block.createCuboidShape(1f, 5f, 7f, 3f, 8f, 9f));
            spikes.add(Block.createCuboidShape(3f, 7f, 7f, 4f, 10f, 9f));
            spikes.add(Block.createCuboidShape(7f, 5f, 13f, 9f, 8f, 15f));
            spikes.add(Block.createCuboidShape(7f, 7f, 12f, 9f, 10f, 13f));
            spikes.add(Block.createCuboidShape(13f, 5f, 7f, 15f, 8f, 9f));
            spikes.add(Block.createCuboidShape(12f, 7f, 7f, 13f, 10f, 9f));

            if (singleBlock) {
                spikes = spikes.stream()
                    .map(voxelShape -> voxelShape.offset(0, 3 / 16f, 0))
                    .toList();
            }

            shapes.addAll(spikes);
        }

        return VoxelShapes.union(shapes.getFirst(), shapes.subList(1, shapes.size()).toArray(VoxelShape[]::new));
    }

    // Cursed work around for issues with BlockState setup
    private static WaystoneShape cursedField_shape = WaystoneShape.NORMAL;

    private final WaystoneShape shape;

    private WaystoneBlock(Settings settings, WaystoneShape shape) {
        super(settings);

        this.shape = shape;

        var defaultState = getStateManager().getDefaultState();

        if (shape.equals(WaystoneShape.NORMAL)) {
            defaultState = defaultState.with(HALF, DoubleBlockHalf.LOWER);
        }

        defaultState = defaultState
            .with(FACING, Direction.NORTH)
            .with(WATERLOGGED, false)
            .with(GENERATED, false);

        setDefaultState(defaultState);

        WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY.addSupportedBlock(this);
    }

    public static WaystoneBlock of(Settings settings, WaystoneShape shape) {
        cursedField_shape = shape;
        return new WaystoneBlock(settings, shape);
    }

    public WaystoneShape shape() {
        return shape;
    }

    @Nullable
    public static WaystoneBlockEntity getEntity(BlockView world, BlockPos pos) {
        var state = world.getBlockState(pos);

        return (state.getBlock() instanceof WaystoneBlock)
            ? getEntity(world, pos, state)
            : null;
    }

    public static WaystoneBlockEntity getEntity(BlockView world, BlockPos pos, BlockState state) {
        pos = getBasePos(pos, state);

        return world.getBlockEntity(pos, WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY).orElse(null);
    }

    public MapCodec<WaystoneBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return !state.contains(HALF) || state.get(HALF) == DoubleBlockHalf.LOWER
            ? new WaystoneBlockEntity(pos, state)
            : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(
            type,
            WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY,
            (world1, pos, state1, blockEntity) -> {
                if (world1.isClient) {
                    blockEntity.tickClient(world, pos, state1);
                } else {
                    blockEntity.tickServer(world, pos, state1);
                }
            }
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        var properties = (cursedField_shape.equals(WaystoneShape.NORMAL))
            ? new Property[]{HALF, FACING, WATERLOGGED, GENERATED}
            : new Property[]{FACING, WATERLOGGED, GENERATED};

        stateManager.add(properties);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var bottomState = world.getBlockState(pos);
        var config = CONFIG;

        // TODO: HAVE SUCH BE REALLY REALLY REALLY HARD TO BREAK AND PREVENT DROPPING
        if (config.unbreakableGeneratedWaystones() && state.get(GENERATED)) return 0;

        if (bottomState.isOf(this)) {
            BlockPos entityPos = getBasePos(pos, bottomState);
            if (world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone) {
                if (!player.hasPermissionLevel(4)) {
                    switch (config.breakingWaystonePermission()) {
                        case OWNER -> {
                            var data = waystone.getData();
                            if (data instanceof NetworkedWaystoneData networkedData) {
                                var owner = networkedData.ownerID();
                                if (owner != null && !player.getUuid().equals(owner)) return 0;
                            }
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
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        if (!state.contains(HALF)) return OUTLINE_SHAPE_SINGLE;
        return state.get(HALF) == DoubleBlockHalf.LOWER ? OUTLINE_SHAPE_BOTTOM : OUTLINE_SHAPE_TOP;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (shape) {
            case NORMAL -> state.get(HALF) == DoubleBlockHalf.LOWER ? COLLISION_SHAPE_BOTTOM : COLLISION_SHAPE_TOP;
            case SMALL -> COLLISION_SHAPE_SINGLE;
            case MINI -> VoxelShapes.union(
                Block.createCuboidShape(1f, 0f, 1f, 15f, 1f, 15f),
                Block.createCuboidShape(2f, 1f, 2f, 14f, 2f, 14f),
                Block.createCuboidShape(3f, 2f, 3f, 13f, 3f, 13f)
            );
        };
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(getBasePos(pos, state)) instanceof WaystoneBlockEntity waystone) {
            var stack = new ItemStack(this.asItem());
            stack.applyComponentsFrom(waystone.createComponentMap());
            return stack;
        }
        return super.getPickStack(world, pos, state);
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
        if (!world.isClient && shape().equals(WaystoneShape.NORMAL) && (player.isCreative() || !player.canHarvest(state))) {
            TallBlantBlockAccessor.fWaystones$onBreakInCreative(world, pos, state, player);
        }

//        @Nullable BlockPos topPos;
//        BlockPos botPos;
//
//        if (state.contains(HALF)) {
//            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
//                topPos = pos;
//                botPos = pos.down();
//            } else {
//                topPos = pos.up();
//                botPos = pos;
//            }
//        } else {
//            topPos = null;
//            botPos = pos;
//        }


        if (world.getBlockEntity(pos) instanceof WaystoneBlockEntity waystone) {
            if (!world.isClient) {
                var itemStack = new ItemStack(state.getBlock().asItem());

                if (!player.isCreative()) {
                    itemStack.applyComponentsFrom(waystone.createComponentMap());
                    waystone.spawnItemStackAbove(itemStack);
                }

                var controllerStack = waystone.exportControllerStack();
                waystone.spawnItemStackAbove(controllerStack);

                if (!waystone.getInventory().isEmpty()) {
                    ItemScatterer.spawn(world, waystone.getPos().up(2), waystone.getInventory());
                    waystone.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
                }

                var mossStack = waystone.removeMoss();
                waystone.spawnItemStackAbove(mossStack);

                if (waystone.getData() != null) {
                    var uuid = waystone.getUUID();

                    if (uuid != null) {
                        WaystoneDataStorage.getStorage(world).removePosition(uuid);
                    }
                }
            } else {
                waystone.generateLoot(player);
            }
        }

//        var breakState = super.onBreak(world, pos, state, player);
//
//        world.removeBlock(botPos, false);
//
//        if (topPos != null) {
//            world.removeBlock(topPos, false);
//            world.updateNeighbors(topPos, Blocks.AIR);
//        }
//
//        return breakState;
        return super.onBreak(world, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var blockPos = ctx.getBlockPos();
        var world = ctx.getWorld();
        var fluidState = world.getFluidState(blockPos);
        if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
            var state = this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(GENERATED, false);

            if (state.contains(HALF)) {
                state.with(HALF, DoubleBlockHalf.LOWER);
            }

            return state;
        } else {
            return null;
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.contains(HALF)) {
            var fluidState = world.getFluidState(pos.up());

            world.setBlockState(
                pos.up(),
                state
                    .with(HALF, DoubleBlockHalf.UPPER)
                    .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER),
                Block.NOTIFY_ALL
            );
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return !state.contains(HALF) || state.get(HALF).equals(DoubleBlockHalf.LOWER)
            ? BlockRenderType.MODEL
            : BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!entity.bypassesLandingEffects()) {
            var waystone = WaystoneBlock.getEntity(world, entity.getLandingPos());

            if (waystone != null && waystone.controllerStack().isIn(ConventionalItemTags.STORAGE_BLOCKS_SLIME)) {
                this.bounce(entity);

                return;
            }
        }

        super.onEntityLand(world, entity);
    }

    private void bounce(Entity entity) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.y < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(vec3d.x, -vec3d.y * d, vec3d.z);
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        var waystone = WaystoneBlock.getEntity(world, pos);
        if (waystone != null) waystone.setupForTeleport(entity);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var hand = player.getActiveHand();
        var stack = player.getStackInHand(hand);
        var item = stack.getItem();

        if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) return ActionResult.PASS;
        if ((player.isSneaking() || !stack.contains(WaystoneDataComponents.HASH_TARGET)) && stack.isIn(LOCAL_VOID_ITEMS)) return ActionResult.PASS;
        if (item instanceof WaystoneDebuggerItem) return ActionResult.PASS;

        var blockEntity = WaystoneBlock.getEntity(world, pos, state);
        if (blockEntity == null) return ActionResult.FAIL;

        var result = ActionResult.PASS;

        if (blockEntity.getControllerStack().isEmpty()) {
            if (!stack.isEmpty() && !player.isSneaking()) {
                blockEntity.swapControllerStack(player, hand);

                result = ActionResult.SUCCESS;
            }
        } else if (player.getStackInHand(player.getActiveHand()).isOf(Items.FILLED_MAP)) {
            return ActionResult.PASS;
        } else {
            var viningResults = blockEntity.attemptMossingInteraction(player, hand);

            if (viningResults.isAccepted()) return viningResults;
        }

        var storage = WaystoneDataStorage.getStorage(player);
        WaystoneData data = blockEntity.getData();

        if (data == null || result.isAccepted()) return result;

        if (blockEntity.controllerStack().isIn(DIRECTED_TELEPORT_ITEMS)) {
            var target = WaystoneHashTarget.get(blockEntity.controllerStack(), world);

            if (target != null) {
                var returnStack = WaystoneComponentEventHooks.attemptTeleport(target, world, player, blockEntity.controllerStack());

                return ActionResult.SUCCESS;
            }
        }

        if (item instanceof DyeItem dyeItem) {
            var color = dyeItem.getColor().getSignColor();
            if (data.color() != color) {
                if (world.isClient) {
                    ItemOps.decrementPlayerHandItem(player, hand);

                    storage.recolorWaystone(data.uuid(), color);

                    world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                return ActionResult.SUCCESS;
            }
        }

        if (stack.isIn(WAYSTONE_CLEANERS)) {
            var type = blockEntity.getWaystoneType();

            if (blockEntity.getColor() != type.defaultRuneColor()) {
                if (world.isClient) {
                    storage.recolorWaystone(data.uuid(), type.defaultRuneColor());

                    if (stack.isIn(WAYSTONE_BUCKET_CLEANERS)) {
                        if (world.getRandom().nextInt(100) == 69) {
                            world.playSound(null, pos, WAYSTONE_CLEAN_BUCKET_STEAL, SoundCategory.BLOCKS, 1.0F, 1.0F);

                            player.sendEquipmentBreakStatus(item, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

                            ItemOps.decrementPlayerHandItem(player, hand);
                        } else {
                            world.playSound(null, pos, WAYSTONE_CLEAN_BUCKET, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    } else {
                        world.playSound(null, pos, WAYSTONE_CLEAN_SPONGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        if (world.isClient) return ActionResult.SUCCESS;

        if (data instanceof NetworkedWaystoneData networkedData) {
            if (player.isSneaking() && (player.hasPermissionLevel(2) || (CONFIG.allowOwnersToRedeemPayments() && player.getUuid().equals(networkedData.ownerID())))) {
                if (!blockEntity.getInventory().isEmpty()) {
                    blockEntity.spawnItemStackAbove(blockEntity.getInventory());

                    blockEntity.setInventory(DefaultedList.ofSize(0, ItemStack.EMPTY));
                    return ActionResult.SUCCESS;
                }
            }

            var playerData = WaystonePlayerData.getData(player);

            var discovered = playerData.discoveredWaystones();
            if (!discovered.contains(blockEntity.getUUID())) {
                if (!networkedData.global()) {
                    var discoverItemId = Utils.getDiscoverItem();

                    if (!player.isCreative()) {
                        var discoverItem = Registries.ITEM.get(discoverItemId);
                        int discoverAmount = CONFIG.requiredDiscoveryAmount();

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
                        networkedData.name()
                    ), false);
                }

                playerData.discoverWaystone(blockEntity.getUUID());
            }

            if (networkedData.ownerID() == null) {
                var uuid = blockEntity.getUUID();

                storage.setOwner(uuid, player);
            }
        }

        if (result == ActionResult.PASS && blockEntity.getControllerStack().isIn(VALID_NETWORK_CONNECTORS)) {
            var screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) player.openHandledScreen(screenHandlerFactory);
        }

        return ActionResult.success(false);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return super.createScreenHandlerFactory(state, world, getBasePos(pos, state));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockPos newPos;
        DoubleBlockHalf verticalPosition;

        if (!state.isOf(this)) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        if (state.contains(HALF)) {
            if (state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) {
                newPos = pos.down();
                verticalPosition = DoubleBlockHalf.LOWER;
            } else {
                newPos = pos.up();
                verticalPosition = DoubleBlockHalf.UPPER;
            }
        } else {
            newPos = null;
            verticalPosition = null;
        }

        if (!(newState.getBlock() instanceof WaystoneBlock)) {
            var waystone = getEntity(world, pos, state);

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

            var adjustedState = newState.with(WATERLOGGED, fluid);

            if (verticalPosition != null) {
                adjustedState = adjustedState.with(WaystoneBlock.HALF, verticalPosition);
            }

            world.setBlockState(newPos, adjustedState);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));

        if (state.contains(HALF)) {
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

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return !state.contains(HALF)
               || state.get(HALF) == DoubleBlockHalf.LOWER
               || isCorrectOtherHalf(state, world.getBlockState(pos.down()));
    }

    public static BlockPos getBasePos(BlockPos pos, BlockState state) {
        return state.contains(HALF) && state.get(HALF) == DoubleBlockHalf.UPPER
            ? pos.down()
            : pos;
    }

    protected boolean isCorrectOtherHalf(BlockState state, BlockState other) {
        return state.isOf(this) && other.isOf(this) &&
               state.get(HALF).getOtherHalf().equals(other.get(HALF)) &&
               state.get(FACING).equals(other.get(FACING)) &&
               state.get(GENERATED).equals(other.get(GENERATED));
    }
}
