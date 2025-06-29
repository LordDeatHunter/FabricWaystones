package wraith.fwaystones.block;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.*;
import wraith.fwaystones.api.core.*;
import wraith.fwaystones.api.moss.MossType;
import wraith.fwaystones.api.moss.MossTypes;
import wraith.fwaystones.api.teleport.TeleportAction;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.client.screen.ExperimentalWaystoneScreenHandler;
import wraith.fwaystones.item.components.WaystoneTyped;
import wraith.fwaystones.particle.RuneParticleEffect;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.item.components.WaystoneDataHolder;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.registry.WaystoneItems;
import wraith.fwaystones.util.*;

import java.util.List;
import java.util.UUID;

import static wraith.fwaystones.FabricWaystones.WAYSTONE_MOSS_APPLY;
import static wraith.fwaystones.FabricWaystones.WAYSTONE_SHEAR;

public class WaystoneBlockEntity extends LootableContainerBlockEntity implements SidedInventory, ExtendedScreenHandlerFactory<WaystoneScreenOpenDataPacket>, WaystoneAccess {

    public Quaternionf controllerRotation;
    public Quaternionf lastControllerRotation;
    public int ticks;

    @Nullable
    public Integer focusedEntityId = null;
    @Nullable
    public Vec3d focusVector = null;

    private static final KeyedEndec<Integer> FOCUSED_ENTITY_KEY = Endec.INT.nullableOf().keyed("focusedEntity", () -> null);
    private static final KeyedEndec<Vec3d> FOCUS_VECTOR_KEY = MinecraftEndecs.VEC3D.nullableOf().keyed("focusVector", () -> Vec3d.ZERO);

    private static final Random RANDOM = Random.create();

    //--

    private WaystonePosition waystonePosition;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);

    @Nullable
    public WaystoneDataHolder dataHolder = null;

    private static final KeyedEndec<Identifier> WAYSTONE_TYPE_ID_KEY = MinecraftEndecs.IDENTIFIER.keyed("waystone_type", () -> WaystoneTypes.STONE);
    private Identifier waystoneTypeId = WaystoneTypes.STONE;

    private ItemStack controllerStack = ItemStack.EMPTY;

    private static final KeyedEndec<Identifier> MOSS_TYPE_ID_KEY = MinecraftEndecs.IDENTIFIER.keyed("moss_type", () -> MossTypes.EMPTY_ID);
    private Identifier mossTypeId = MossTypes.EMPTY_ID;

    private ItemStack mossStack = ItemStack.EMPTY;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY, pos, state);
    }


    public int getColor() {
        var data = getData();

        if (data != null) {
            return data.color();
        }

        // TODO: ADD SOMETHING HERE TO HANDLE OTHER STUFF
        return this.getWaystoneType().defaultRuneColor();
    }

    public ItemStack controllerStack() {
        return controllerStack;
    }

    public ItemStack swapControllerStack(ItemStack stack) {
        var returnStack = ItemStack.EMPTY;

        if (!world.isClient) {
            if (!controllerStack.isEmpty()) {
                this.spawnItemStackAbove(exportControllerStack());
            }

            returnStack = importControllerStack(stack);

            this.markDirty();
        }

        return returnStack;
    }

    public ItemStack exportControllerStack() {
        var currentStack = this.controllerStack;

        this.controllerStack = ItemStack.EMPTY;

        if (currentStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            var storage = WaystoneDataStorage.getStorage(this.world);

            var data = storage.getData(this.position());

            if (data != null) {
                currentStack.set(WaystoneDataComponents.DATA_HOLDER, new WaystoneDataHolder(data));
            }

            storage.removePositionAndData(this);
        }

        this.markDirty();

        return currentStack;
    }

    private ItemStack importControllerStack(ItemStack stack) {
        if (stack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            var holder = stack.get(WaystoneDataComponents.DATA_HOLDER);

            if (holder != null) {
                stack.remove(WaystoneDataComponents.DATA_HOLDER);

                this.dataHolder = holder;
            }
        }

        this.controllerStack = stack.split(1);

        return stack;
    }

    public void spawnItemStackAbove(ItemStack stack) {
        var dropPos = this.pos.up(1).toCenterPos();
        ItemScatterer.spawn(world, dropPos.getX(), dropPos.getY(), dropPos.getZ(), stack);
    }

    //--

    @Nullable
    public NetworkedWaystoneData getData() {
        if (this.controllerStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            return WaystoneDataStorage.getStorage(this.world).getData(this.position());
        }

        return null;
    }

    public ItemStack getControllerStack() {
        return this.controllerStack;
    }

    @Nullable
    public UUID getUUID() {
        var uuid = WaystoneDataStorage.getStorage(this.world).getUUID(this.position());

        if (uuid == null) return WaystoneData.EMPTY_UUID;

        return uuid;
    }

    public WaystoneType getWaystoneType() {
        return WaystoneTypes.getTypeOrDefault(this.waystoneTypeId);
    }

    public TeleportAction createNetworkTeleport(TeleportSource source) {
        return TeleportAction.networkTeleport(this.getUUID(), source);
    }

    //--

    @Nullable
    public MossType getMossType() {
        return !this.mossTypeId.equals(MossTypes.EMPTY_ID)
            ? MossTypes.getTypeOrDefault(this.mossTypeId)
            : null;
    }

    public boolean isMossy() {
        return getMossType() != null;
    }

    public ItemStack removeMoss() {
        var mossStack = this.mossStack;

        this.mossStack = ItemStack.EMPTY;
        this.mossTypeId = MossTypes.EMPTY_ID;

        return mossStack;
    }

    //--

    public boolean isActive() {
        if (this.controllerStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            var data = getData();

            if (data == null) return false;

            return data.hasOwner();
        }

        return false;
    }

    public TypedActionResult<ItemStack> attemptMossingInteraction(BlockPos pos, ItemStack stack) {
        var mossType = MossTypes.getMossType(stack);

        if (mossType != null) {
            if (mossType.getId().equals(this.mossTypeId)) return TypedActionResult.pass(ItemStack.EMPTY);

            this.mossTypeId = mossType.getId();

            if (!world.isClient) {
                var prevMoss = this.mossStack;

                world.playSound(null, pos, WAYSTONE_MOSS_APPLY, SoundCategory.BLOCKS, 1.0F, 1.0F);

                markDirty();

                this.mossStack = stack.copy();

                return TypedActionResult.success(prevMoss);
            }

            return TypedActionResult.success(ItemStack.EMPTY);
        } else if (stack.isIn(ConventionalItemTags.SHEAR_TOOLS) && !this.mossTypeId.equals(MossTypes.EMPTY_ID)) {
            this.mossTypeId = MossTypes.EMPTY_ID;

            if (!world.isClient) {
                var prevMoss = this.mossStack;

                world.playSound(null, pos, WAYSTONE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);

                markDirty();

                this.mossStack = ItemStack.EMPTY;

                return TypedActionResult.success(prevMoss);
            }

            return TypedActionResult.success(ItemStack.EMPTY);
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    public WaystonePosition position() {
        if (this.waystonePosition == null) createHash(world, pos);

        return this.waystonePosition;
    }

    public void createHash(World world, BlockPos pos) {
        this.waystonePosition = new WaystonePosition(world.getRegistryKey(), pos);
        markDirty();
    }

    //--

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ExperimentalWaystoneScreenHandler(syncId, playerInventory);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return createMenu(syncId, playerInventory, playerInventory.player);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container." + FabricWaystones.MOD_ID + ".waystone");
    }

    @Override
    protected Text getContainerName() {
        return getDisplayName();
    }

    @Override
    public WaystoneScreenOpenDataPacket getScreenOpeningData(ServerPlayerEntity player) {
        return new WaystoneScreenOpenDataPacket(this.position(), this.canAccess(player));
    }

    //--

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(List<ItemStack> newInventory) {
        if (newInventory instanceof DefaultedList<ItemStack> defaultedList) {
            this.inventory = defaultedList;
        } else {
            this.inventory = DefaultedList.ofSize(newInventory.size(), ItemStack.EMPTY);

            for (int i = 0; i < newInventory.size(); ++i) {
                this.inventory.set(i, newInventory.get(i));
            }
        }

        markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    //--

    @Override
    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(tag, lookup);

        this.inventory = DefaultedList.ofSize(tag.getInt("inventory_size"), ItemStack.EMPTY);

        Inventories.readNbt(tag, inventory, lookup);

        this.waystoneTypeId = tag.get(WAYSTONE_TYPE_ID_KEY);
        this.controllerStack = ItemStack.fromNbtOrEmpty(lookup, tag.getCompound("controller_stack"));

        this.mossTypeId = tag.get(MOSS_TYPE_ID_KEY);

        this.mossStack = ItemStack.fromNbtOrEmpty(lookup, tag.getCompound("moss_stack"));

        this.focusedEntityId = tag.get(FOCUSED_ENTITY_KEY);
        this.focusVector = tag.get(FOCUS_VECTOR_KEY);

        // Attempts to force an update of the states for re-rendering
        if (this.world != null && this.world.isClient) {
            world.scheduleBlockRerenderIfNeeded(pos.up(), Blocks.AIR.getDefaultState(), world.getBlockState(pos.up()));
            world.scheduleBlockRerenderIfNeeded(pos, Blocks.AIR.getDefaultState(), world.getBlockState(pos));
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(tag, lookup);

        tag.putInt("inventory_size", this.inventory.size());

        Inventories.writeNbt(tag, this.inventory, lookup);

        tag.put(WAYSTONE_TYPE_ID_KEY, this.waystoneTypeId);
        tag.put("controller_stack", this.controllerStack.encodeAllowEmpty(lookup));

        tag.put(MOSS_TYPE_ID_KEY, this.mossTypeId);
        tag.put("moss_stack", this.mossStack.encodeAllowEmpty(lookup));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var tag = super.toInitialChunkDataNbt(registryLookup);

        tag.put(WAYSTONE_TYPE_ID_KEY, this.waystoneTypeId);
        tag.put("controller_stack", this.controllerStack.encodeAllowEmpty(registryLookup));

        tag.put(MOSS_TYPE_ID_KEY, this.mossTypeId);

        //        if (this.focusedEntityId != null)
        tag.put(FOCUSED_ENTITY_KEY, this.focusedEntityId);
//        if (this.focusVector != null)
        tag.put(FOCUS_VECTOR_KEY, this.focusVector);

        return tag;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);

        componentMapBuilder.add(DataComponentTypes.CONTAINER, null);

//        var storage = WaystoneDataStorage.getStorage(this.world);
//        if (FabricWaystones.CONFIG.allowSavingWaystoneData()) {
//            var holder = storage.removePositionAndExport(this);
//
//            if (holder != null) {
//                componentMapBuilder.add(WaystoneDataComponents.DATA_HOLDER, holder);
//            }
//        } else {
//            storage.removePositionAndData(this);
//        }

        componentMapBuilder.add(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(this.waystoneTypeId));
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);

        this.dataHolder = components.get(WaystoneDataComponents.DATA_HOLDER);

        if (this.dataHolder != null) {
            WaystoneDataStorage.getStorage(this.world).createGetOrImportData(this);
        }

        this.waystoneTypeId = components.getOrDefault(WaystoneDataComponents.WAYSTONE_TYPE, WaystoneTyped.DEFAULT).id();
    }

    //--

    public boolean teleportPlayer(PlayerEntity player, TeleportSource source, boolean takeCost) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        return Utils.teleportPlayer(serverPlayer, createNetworkTeleport(source), takeCost);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(world, pos);
    }

    //--

    public static void tickServer(World world, BlockPos pos, BlockState state, WaystoneBlockEntity waystone) {
        if (world.isClient) return;
        var controller = waystone.getControllerPos();
        var update = RANDOM.nextInt(100) == 0;

        Entity nextTarget = world.getClosestPlayer(controller.x, controller.y, controller.z, 4.5, waystone::shouldWatchEntity);
        Vec3d nextVector = getRandomDirection();
        if (nextTarget == null) {
            var nearbyEntities = world.getOtherEntities(
                null,
                Box.from(controller).expand(10),
                EntityPredicates.EXCEPT_SPECTATOR
            );
            if (!nearbyEntities.isEmpty() && RANDOM.nextFloat() >= 0.3) {
                nextTarget = nearbyEntities.get(RANDOM.nextInt(nearbyEntities.size()));
            } else {
                var hit = world.raycast(
                    new RaycastContext(
                        controller,
                        controller.add(nextVector.multiply(10)),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        ShapeContext.absent()
                    )
                );
                if (hit.getType() == HitResult.Type.BLOCK) {
                    world.setBlockBreakingInfo(
                        -1,
                        hit.getBlockPos(),
                        100
                    );
                }
            }
        } else {
            update = true;
            waystone.shootRuneAt(nextTarget);
            waystone.suckPortalParticleFrom(nextTarget);
        }
        if (nextTarget != null) nextVector = nextTarget.getPos();
        if (update) {
            waystone.focusedEntityId = nextTarget != null ? nextTarget.getId() : null;
            waystone.focusVector = nextVector;
            waystone.markDirty();
        }

        world.getOtherEntities(
            null,
            Box.from(controller).expand(6),
            waystone::shouldWatchEntity
        ).forEach(waystone::shootRuneAt);

        waystone.suckARandomPortalParticle();

//        Entity adhdTarget = ;
//        if (adhdTarget == null) {
//            var adhdRandom = Random.create(waystone.ADHD_SEED + world.getTime());
//            var distractions = world.getOtherEntities(
//                null,
//                Box.of(controller, 20, 20, 20),
//                EntityPredicates.EXCEPT_SPECTATOR
//            );
//            if (distractions.isEmpty()) {
//                adhdTarget = null;
//            } else {
//                adhdTarget = distractions.get(world.random.nextInt(distractions.size()));
//            }
//        }
//
//        if (nextTarget != null) {
//            var offset = nextTarget.getEyePos().subtract(controller).normalize();
//            waystone.targetControllerRotation = new Quaternionf().lookAlong(offset.toVector3f(), new Vector3f(0, 1, 0));
//        } else {
//            waystone.targetControllerRotation += 0.02f;
//        }
//
//        while (waystone.controllerRotation >= Math.PI) waystone.controllerRotation -= (float) Math.TAU;
//        while (waystone.controllerRotation < -Math.PI) waystone.controllerRotation += (float) Math.TAU;
//
//        while (waystone.targetControllerRotation >= Math.PI) waystone.targetControllerRotation -= (float) Math.TAU;
//        while (waystone.targetControllerRotation < -Math.PI) waystone.targetControllerRotation += (float) Math.TAU;
//
//        var nextRotation = waystone.targetControllerRotation - waystone.controllerRotation;
//
//        while (nextRotation >= Math.PI) nextRotation -= (float) Math.TAU;
//        while (nextRotation < -Math.PI) nextRotation += (float) Math.TAU;
//
//        waystone.controllerRotation += nextRotation * 0.4f;
//        waystone.ticks++;
    }

    @Environment(EnvType.CLIENT)
    public static void tickClient(World world, BlockPos pos, BlockState state, WaystoneBlockEntity waystone) {
        waystone.lastControllerRotation = waystone.controllerRotation;
        var controller = waystone.getControllerPos();

        var target = waystone.focusedEntityId != null ? world.getEntityById(waystone.focusedEntityId) : null;

        var targetVector = waystone.focusVector;
        if (target != null) targetVector = target.getPos().subtract(controller).normalize();
        if (targetVector == null) targetVector = getRandomDirection();

        waystone.controllerRotation = new Quaternionf().lookAlong(
            targetVector.toVector3f(),
            new Vector3f(0, 1, 0)
        );
        waystone.ticks++;
    }

    private void shootRuneAt(Entity target) {
        if (world == null || !this.isActive()) return;
        if (RANDOM.nextInt(20) != 0) return;
        var basePos = this.getPos().toBottomCenterPos();
        var watcherPos = basePos.add(0, getControllerHeight(), 0);
        var targetPos = target.getPos();
        var distanceCheck = Double.compare(Math.abs(watcherPos.x - targetPos.x), Math.abs(watcherPos.z - targetPos.z));
        var start = targetPos.add(0, 1.25, 0);
        var end = basePos
            .subtract(start)
            .add(
                distanceCheck > 0 ? Double.compare(targetPos.x, watcherPos.x) * 0.4 : 0,
                getEmitterRunesHeight(),
                distanceCheck < 0 ? Double.compare(targetPos.z, watcherPos.z) * 0.4 : 0
            );
        this.world.addParticle(
            new RuneParticleEffect(getColor()),
            start.x, start.y, start.z,
            end.x, end.y, end.z
        );
    }

    private void suckPortalParticleFrom(Entity target) {
        if (world == null || !this.isActive()) return;
        if (RANDOM.nextInt(40) != 0) return;
        var watcherPos = this.getPos().toBottomCenterPos().add(0, getControllerHeight(), 0);
        var bb = target.getBoundingBox();
        var bbMin = bb.getMinPos();
        var bbMax = bb.getMaxPos();
        var end = new Vec3d(
            bbMin.x + (bbMax.x - bbMin.x) * RANDOM.nextDouble(),
            bbMin.y + (bbMax.y - bbMin.y) * RANDOM.nextDouble(),
            bbMin.z + (bbMax.z - bbMin.z) * RANDOM.nextDouble()
        )
            .subtract(watcherPos)
            .subtract(0, 0.75, 0);
        this.world.addParticle(
            ParticleTypes.PORTAL,
            watcherPos.x, watcherPos.y, watcherPos.z,
            end.x, end.y, end.z
        );
    }

    private void suckARandomPortalParticle() {
        if (world == null || !this.isActive()) return;
        if (RANDOM.nextInt(100) != 0) return;

        var controllerPos = getControllerPos();
        var randomDirection = getRandomDirection();
        this.world.addParticle(
            ParticleTypes.PORTAL,
            controllerPos.x,
            controllerPos.y,
            controllerPos.z,
            randomDirection.x * 2,
            randomDirection.y * 2 - 0.2,
            randomDirection.z * 2
        );
    }

    private static Vec3d getRandomDirection() {
        double theta = RANDOM.nextDouble() * 2 * Math.PI;
        double u = RANDOM.nextDouble();
        double phi = Math.acos(2 * u - 1);
        return new Vec3d(
            Math.sin(phi) * Math.cos(theta),
            Math.sin(phi) * Math.sin(theta),
            Math.cos(phi)
        );
    }

    public boolean isSingleBlock() {
        return ((WaystoneBlock) this.getCachedState().getBlock()).singleBlock();
    }

    public double getControllerHeight() {
        return (isSingleBlock() ? 14 : 29) / 16f;
    }

    public Vec3d getControllerPos() {
        return this.pos.toBottomCenterPos().add(0, getControllerHeight(), 0);
    }

    public double getEmitterRunesHeight() {
        return (isSingleBlock() ? 5 : 20) / 16f - 0.05f;
    }

    private boolean shouldWatchEntity(@Nullable Entity entity) {
        if (entity instanceof PlayerEntity player && EntityPredicates.EXCEPT_SPECTATOR.test(player)) {
            var uuid = getUUID();

            if (uuid != null) {
                var data = WaystonePlayerData.getData(player);

                return data.hasDiscoverdWaystone(uuid);
            } else {
                return !this.controllerStack.isEmpty();
            }
        }

        return false;
    }

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo(this.pos.toCenterPos()) <= 64.0D;
    }
}
