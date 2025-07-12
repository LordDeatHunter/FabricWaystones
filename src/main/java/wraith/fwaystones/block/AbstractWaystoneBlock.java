package wraith.fwaystones.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.Ownable;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.item.components.WaystoneHashTarget;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static wraith.fwaystones.FabricWaystones.*;

@SuppressWarnings("deprecation")
public abstract class AbstractWaystoneBlock extends BlockWithEntity implements Waterloggable {
    private final int height;

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty GENERATED = BooleanProperty.of("generated");

    public AbstractWaystoneBlock(int height, Settings settings) {
        super(settings);
        this.height = height;
        this.setDefaultState(
            this.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(GENERATED, false)
                .with(WATERLOGGED, false)
        );

        WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY.addSupportedBlock(this);
    }

    //--

    public BlockPos getBasePos(BlockPos pos, BlockState state) {
        return pos;
    }

    public double getHeight() {
        return height;
    }

    public double getControllerY() {
        return getHeight() + 6;
    }

    public Vec3d getTopPos(BlockPos basePos) {
        return basePos.toBottomCenterPos().add(0, getHeight() / 16f, 0);
    }

    public Vec3d getControllerPos(BlockPos basePos) {
        return basePos.toBottomCenterPos().add(0, getControllerY() / 16f, 0);
    }

    public double getEmitterRunesHeight() {
        var value = getHeight() - 1;

        return value / 16f - 0.05f;
    }

    public Box getTeleportBox(BlockPos basePos) {
        var boxSize = 10 / 16f;
        var boxPos = getTopPos(basePos)
            .offset(Direction.UP, 5 / 16f);

        return Box.of(boxPos, boxSize, boxSize, boxSize);
    }

    //--

    @Override
    public MapCodec<AbstractWaystoneBlock> getCodec() {
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return super.createScreenHandlerFactory(state, world, getBasePos(pos, state));
    }

    public void scheduleBlockRerender(World world, BlockPos pos) {
        world.scheduleBlockRerenderIfNeeded(pos, Blocks.AIR.getDefaultState(), world.getBlockState(pos));
    }

    @Nullable
    public Vec3d findTeleportPosition(Entity entity, CollisionView world, BlockPos pos, Direction waystoneDirection) {
        var valid = findTeleportPosition(entity, world, pos, waystoneDirection, true);
        if (valid != null) return valid;
        return findTeleportPosition(entity, world, pos, waystoneDirection, false);
    }

    @Nullable
    public Vec3d findTeleportPosition(Entity entity, CollisionView world, BlockPos pos, Direction waystoneDirection, boolean ignoreInvalidPos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Vec3i offset : getTeleportOffsets(waystoneDirection)) {
            mutable.set(pos).move(offset);
            var vec3d = Dismounting.findRespawnPos(entity.getType(), world, mutable, ignoreInvalidPos);
            if (vec3d != null) return vec3d;
        }

        return null;
    }

    public abstract List<Vec3i> getTeleportOffsets(Direction waystoneDirection);

    public static List<Vec3i> getHorizontalTeleportOffsets(Direction waystoneDirection) {
        var locations = new ArrayList<Vec3i>();
        for (int i = 0; i < 4; i++) {
            locations.add(new Vec3i(waystoneDirection.getOffsetX(), 0, waystoneDirection.getOffsetZ()));
            waystoneDirection = waystoneDirection.rotateYClockwise();
        }
        return locations;
    }

    public float getTeleportationYaw(Vec3d teleportPos, BlockPos waystonePos, BlockState state) {
        var offset = teleportPos.subtract(Vec3d.ofCenter(waystonePos));
        if (offset.x == 0 && offset.z == 0) return state.get(FACING).getOpposite().asRotation();
        return (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(offset.x, offset.z)));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return getBasePos(pos, state) == pos
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
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
            .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
            .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER)
            .with(GENERATED, false);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!entity.bypassesLandingEffects()) {
            var waystone = getEntity(world, entity.getLandingPos());

            if (waystone != null && waystone.controllerStack().isIn(ConventionalItemTags.STORAGE_BLOCKS_SLIME)) {
                this.bounce(entity);

                return;
            }
        }

        super.onEntityLand(world, entity);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        var stack = super.getPickStack(world, pos, state);
        if (world.getBlockEntity(getBasePos(pos, state)) instanceof WaystoneBlockEntity waystone) {
            stack.applyComponentsFrom(waystone.createComponentMap());
            return stack;
        }
        return stack;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
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

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, GENERATED, WATERLOGGED);
    }

    @Nullable
    public static WaystoneBlockEntity getEntity(BlockView world, BlockPos pos) {
        var state = world.getBlockState(pos);

        if (state.getBlock() instanceof AbstractWaystoneBlock waystoneBlock) {
            var basePos = waystoneBlock.getBasePos(pos, state);

            if (world.getBlockState(basePos).isOf(waystoneBlock)) {
                return world.getBlockEntity(basePos, WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY).orElse(null);
            }
        }

        return null;
    }

    private void bounce(Entity entity) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.y < 0.0) {
            double d = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(vec3d.x, -vec3d.y * d, vec3d.z);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var hand = player.getActiveHand();
        var stack = player.getStackInHand(hand);
        var item = stack.getItem();

        if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) return ActionResult.PASS;
        if ((player.isSneaking() || !stack.contains(WaystoneDataComponents.HASH_TARGET)) && stack.isIn(FabricWaystones.LOCAL_VOID_ITEMS)) return ActionResult.PASS;
        if (item instanceof WaystoneDebuggerItem) return ActionResult.PASS;

        var blockEntity = getEntity(world, pos);
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
                // TODO: HANDLE RETURN STACK? MUST BE THOUGHT ABOUT
                var returnStack = WaystoneComponentEventHooks.attemptTeleport(target, world, player, blockEntity.controllerStack());

                return ActionResult.SUCCESS;
            }
        }

        if (item instanceof DyeItem dyeItem) {
            var color = dyeItem.getColor().getSignColor();
            if (data.color() != color) {
                if (!world.isClient) {
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
                if (!world.isClient) {
                    storage.recolorWaystone(data.uuid(), type.defaultRuneColor());

                    SoundEvent actionSound = WAYSTONE_CLEAN_SPONGE;

                    if (stack.isIn(WAYSTONE_BUCKET_CLEANERS)) {
                        if (world.getRandom().nextInt(100) == 69) {
                            player.sendEquipmentBreakStatus(item, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

                            ItemOps.decrementPlayerHandItem(player, hand);

                            actionSound = WAYSTONE_CLEAN_BUCKET_STEAL;
                        } else {
                            actionSound = WAYSTONE_CLEAN_BUCKET;
                        }
                    }

                    world.playSound(null, pos, actionSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                return ActionResult.SUCCESS;
            }
        }

        if (world.isClient) return ActionResult.SUCCESS;

        if (data instanceof NetworkedWaystoneData networkedData) {
            if (player.isSneaking() && (player.hasPermissionLevel(2) || (FabricWaystones.CONFIG.allowOwnersToRedeemPayments() && player.getUuid().equals(networkedData.ownerID())))) {
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

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        var config = FabricWaystones.CONFIG;

        // TODO: HAVE SUCH BE REALLY REALLY REALLY HARD TO BREAK AND PREVENT DROPPING
        if (config.unbreakableGeneratedWaystones() && state.get(GENERATED)) return 0;

        var waystone = getEntity(world, pos);

        if (waystone != null && !player.hasPermissionLevel(4)) {
            switch (config.breakingWaystonePermission()) {
                case OWNER -> {
                    if (waystone.getData() instanceof Ownable data && data.isOwner(player)) return 0;
                }
                case OP -> {
                    if (!player.hasPermissionLevel(2)) return 0;
                }
                case NONE -> {
                    return 0;
                }
            }
        }

        return super.calcBlockBreakingDelta(state, player, world, pos);
    }
}
