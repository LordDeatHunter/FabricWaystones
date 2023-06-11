package wraith.fwaystones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.WaystoneScrollItem;
import wraith.fwaystones.registry.BlockEntityRegister;
import wraith.fwaystones.util.Utils;

import static wraith.fwaystones.WaystonesExpectPlatform.pinlibTryUseOnMarkableBlock;

public class WaystoneBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty GENERATED = BooleanProperty.create("generated");
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty MOSSY = BooleanProperty.create("mossy");
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

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

	public WaystoneBlock(Properties properties) {
		super(properties);
		registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(FACING, Direction.NORTH).setValue(MOSSY, false).setValue(WATERLOGGED, false).setValue(ACTIVE, false).setValue(GENERATED, false));
	}

	@Nullable
	public static WaystoneBlockEntity getEntity(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof WaystoneBlock)) {
			return null;
		}
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.below();
		}
		return level.getBlockEntity(pos) instanceof WaystoneBlockEntity waystone ? waystone : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? null : new WaystoneBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, BlockEntityRegister.WAYSTONE_BLOCK_ENTITY.get(), WaystoneBlockEntity::ticker);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
		stateManager.add(HALF, FACING, MOSSY, WATERLOGGED, ACTIVE, GENERATED);
	}

	@Override
	public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		var bottomState = level.getBlockState(pos);
		if (Waystones.CONFIG.worldgen.unbreakable_generated_waystones && state.getValue(GENERATED)) {
			return 0;
		}
		if (bottomState.getBlock() instanceof WaystoneBlock) {
			BlockPos entityPos = bottomState.getValue(WaystoneBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
			switch (Waystones.CONFIG.permission_level_for_breaking_waystones) {
				case OWNER -> {
					if (level.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone && waystone.getOwner() != null && !player.getUUID().equals(waystone.getOwner())) {
						return 0;
					}
				}
				case OP -> {
					if (!player.hasPermissions(2)) {
						return 0;
					}
				}
				case NONE -> {
					return 0;
				}
			}
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos blockPos = ctx.getClickedPos();

		var nbt = ctx.getItemInHand().getTagElement("BlockEntityTag");
		boolean hasOwner = nbt != null && nbt.contains("waystone_owner");
		var world = ctx.getLevel();
		var fluidState = world.getFluidState(blockPos);

		if (blockPos.getY() < world.getHeight() - 1 && world.getBlockState(blockPos.above()).canBeReplaced(ctx)) {
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
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		BlockPos topPos;
		BlockPos botPos;
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			topPos = pos;
			botPos = pos.below();
		} else {
			topPos = pos.above();
			botPos = pos;
		}

		if (level.getBlockEntity(botPos) instanceof WaystoneBlockEntity waystone && !player.isCreative() && player.hasCorrectToolForDrops(level.getBlockState(botPos)) && level instanceof ServerLevel) {
			if (!level.isClientSide) {
				ItemStack itemStack = new ItemStack(state.getBlock().asItem());
				var compoundTag = new CompoundTag();
				waystone.saveAdditional(compoundTag);
				if (Waystones.CONFIG.store_waystone_data_on_sneak_break && player.isCrouching() && !compoundTag.isEmpty()) {
					itemStack.addTagElement("BlockEntityTag", compoundTag);
				}
				Containers.dropItemStack(level, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, itemStack);
				if (waystone.getBlockState().getValue(MOSSY)) {
					Containers.dropItemStack(level, (double) topPos.getX() + 0.5D, (double) topPos.getY() + 0.5D, (double) topPos.getZ() + 0.5D, new ItemStack(Items.VINE));
				}
			} else {
				waystone.unpackLootTable(player);
			}
		}

		level.removeBlock(topPos, false);
		level.removeBlock(botPos, false);
		level.updateNeighborsAt(topPos, Blocks.AIR);

		super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			super.setPlacedBy(level, pos, state, placer, itemStack);
			return;
		}
		var fluidState = level.getFluidState(pos.above());
		level.setBlockAndUpdate(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER));
		BlockEntity entity = level.getBlockEntity(pos);
		if (placer instanceof ServerPlayer && entity instanceof WaystoneBlockEntity waystone) {
			Waystones.STORAGE.tryAddWaystone(waystone);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.sidedSuccess(true);
		}
		BlockPos openPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
		BlockState topState = level.getBlockState(openPos.above());
		BlockState bottomState = level.getBlockState(openPos);
		Item heldItem = player.getItemInHand(hand).getItem();
		if (heldItem == Items.VINE) {
			if (!topState.getValue(MOSSY)) {
				level.setBlockAndUpdate(openPos.above(), topState.setValue(MOSSY, true));
				level.setBlockAndUpdate(openPos, bottomState.setValue(MOSSY, true));
				if (!player.isCreative()) {
					player.getItemInHand(hand).shrink(1);
				}
			}
			return InteractionResult.PASS;
		}
		if (heldItem == Items.SHEARS) {
			if (topState.getValue(MOSSY)) {
				level.setBlockAndUpdate(openPos.above(), topState.setValue(MOSSY, false));
				level.setBlockAndUpdate(openPos, bottomState.setValue(MOSSY, false));
				var dropPos = openPos.above(2);
				Containers.dropItemStack(level, dropPos.getX() + 0.5F, dropPos.getY() + 0.5F, dropPos.getZ() + 0.5F, new ItemStack(Items.VINE));
			}
			return InteractionResult.PASS;
		}
		if (heldItem instanceof WaystoneScrollItem || heldItem instanceof LocalVoidItem || heldItem instanceof WaystoneDebuggerItem) {
			return InteractionResult.PASS;
		}

		var discovered = ((PlayerEntityMixinAccess) player).getDiscoveredWaystones();

		WaystoneBlockEntity blockEntity = (WaystoneBlockEntity) level.getBlockEntity(openPos);
		if (blockEntity == null) {
			return InteractionResult.FAIL;
		}

		if (player.isCrouching() && (player.hasPermissions(2) || (Waystones.CONFIG.can_owners_redeem_payments && player.getUUID().equals(blockEntity.getOwner())))) {
			if (blockEntity.hasStorage()) {
				Containers.dropContents(level, openPos.above(2), blockEntity.getInventory());
				blockEntity.setInventory(NonNullList.withSize(0, ItemStack.EMPTY));
			}
		} else {
			 if (!Waystones.CONFIG.discover_waystone_on_map_use && pinlibTryUseOnMarkableBlock(player.getItemInHand(hand), level, openPos)){
				 return InteractionResult.SUCCESS;
			 }
			Waystones.STORAGE.tryAddWaystone(blockEntity);
			if (!discovered.contains(blockEntity.getHash())) {
				if (!blockEntity.isGlobal()) {
					var discoverItemId = Utils.getDiscoverItem();
					if (!player.isCreative()) {
						var discoverItem = BuiltInRegistries.ITEM.get(discoverItemId);
						var discoverAmount = Waystones.CONFIG.take_amount_from_discover_item;
						if (!Utils.containsItem(player.getInventory(), discoverItem, discoverAmount)) {
							player.displayClientMessage(Component.translatable(
									"fwaystones.missing_discover_item",
									discoverAmount,
									Component.translatable(discoverItem.getDescriptionId()).withStyle(style ->
											style.withColor(TextColor.parseColor(Component.translatable("fwaystones.missing_discover_item.arg_color").getString()))
									)
							), false);
							return InteractionResult.FAIL;
						} else if (discoverItem != Items.AIR) {
							Utils.removeItem(player.getInventory(), discoverItem, discoverAmount);
							player.displayClientMessage(Component.translatable(
									"fwaystones.discover_item_paid",
									discoverAmount,
									Component.translatable(discoverItem.getDescriptionId()).withStyle(style ->
											style.withColor(TextColor.parseColor(Component.translatable("fwaystones.discover_item_paid.arg_color").getString()))
									)
							), false);
						}
					}
					player.displayClientMessage(Component.translatable(
							"fwaystones.discover_waystone",
							Component.literal(blockEntity.getWaystoneName()).withStyle(style ->
									style.withColor(TextColor.parseColor(Component.translatable("fwaystones.discover_waystone.arg_color").getString()))
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
			BlockPos menupos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
			var screenHandlerFactory = state.getMenuProvider(level, pos);
			if (screenHandlerFactory != null){
				BlockEntity entity = level.getBlockEntity(menupos);
				if(entity instanceof WaystoneBlockEntity data) {
					data.writeScreenOpeningData((ServerPlayer) player, screenHandlerFactory);
				}
			}
		}
		blockEntity.setChanged();
		return InteractionResult.sidedSuccess(false);
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		return super.getMenuProvider(state, level, state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
		BlockPos newPos;
		DoubleBlockHalf verticalPosition;

		if (state.getBlock() != this) {
			super.onRemove(state, level, pos, newState, moved);
			return;
		}

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
			BlockEntity entity = level.getBlockEntity(testPos);
			if (!level.isClientSide && entity instanceof WaystoneBlockEntity waystone) {
				Waystones.STORAGE.removeWaystone(waystone);
			}
			level.removeBlockEntity(testPos);
			level.setBlockAndUpdate(newPos, newState);
		} else {
			var fluid = level.getFluidState(newPos).getType() == Fluids.WATER && verticalPosition == DoubleBlockHalf.LOWER;
			level.setBlockAndUpdate(newPos, newState.setValue(WaystoneBlock.HALF, verticalPosition).setValue(WATERLOGGED, fluid));
		}
		super.onRemove(state, level, pos, newState, moved);
	}

	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

}
