package wraith.fwaystones.block;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.LootableContainerBlockEntity;
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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
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

    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::create);

    public Quaternionf controllerRotation;
    public Quaternionf lastControllerRotation;
    public int ticks;

    public int lookTime;
    public Entity focusedEntity = null;

    public Vec3d focusVector = getRandomControllerOffset();

    private static final KeyedEndec<Vec3d> FOCUS_VECTOR_KEY = MinecraftEndecs.VEC3D.keyed("focusVector", () -> Vec3d.ZERO);

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

    public void swapControllerStack(PlayerEntity player, Hand hand) {
        if (world.isClient) return;

        if (!controllerStack.isEmpty()) {
            this.spawnItemStackAbove(exportControllerStack());
        }

        importControllerStack(player, hand);

        this.markDirty();
    }

    public ItemStack exportControllerStack() {
        var currentStack = this.controllerStack;

        this.controllerStack = ItemStack.EMPTY;

        if (currentStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            var storage = getWaystoneStorage();

            var data = storage.getData(this.position());

            if (data != null) {
                currentStack.set(WaystoneDataComponents.DATA_HOLDER, new WaystoneDataHolder(data));
            }

            storage.removePositionAndData(this);
        }

        this.markDirty();

        return currentStack;
    }

    private void importControllerStack(PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);

        if (stack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            var holder = stack.get(WaystoneDataComponents.DATA_HOLDER);

            if (holder != null) {
                if (!player.isCreative()) {
                    stack.remove(WaystoneDataComponents.DATA_HOLDER);
                }

                this.dataHolder = holder;
            }
        }

        this.controllerStack = stack.copyWithCount(1);

        ItemOps.decrementPlayerHandItem(player, hand);
    }

    public void spawnItemStackAbove(ItemStack stack) {
        if (world.isClient) return;
        var dropPos = this.pos.up(1).toCenterPos();
        ItemScatterer.spawn(world, dropPos.getX(), dropPos.getY(), dropPos.getZ(), stack);
    }

    public void spawnItemStackAbove(List<ItemStack> stacks) {
        if (world.isClient) return;

        for (var stack : stacks) {
            var dropPos = this.pos.up(1).toCenterPos();
            ItemScatterer.spawn(world, dropPos.getX(), dropPos.getY(), dropPos.getZ(), stack);
        }
    }


    //--

    @Nullable
    public WaystoneData getData() {
        var storage = getWaystoneStorage();
        var position = position();

        var data = storage.getData(position);

        boolean setupData = false;

        if (this.controllerStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            setupData = !(data instanceof NetworkedWaystoneData);
        }

        if (setupData) {
            var seedData = (this.dataHolder != null) ? this.dataHolder.data() : storage.getData(position);

            if (this.controllerStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
                data = storage.createGetOrImportData(
                    this,
                    (seedData instanceof NetworkedWaystoneData networkedData) ? networkedData : null,
                    (uuid) -> {
                        var customName = this.getCustomName();

                        var name = customName != null ? customName.getString() : "";

                        return new NetworkedWaystoneData(uuid, name);
                    });
            } else {
                data = storage.createGetOrImportData(this, seedData, WaystoneData::new);
            }

            this.dataHolder = null;
        }

        return data;
    }

    public ItemStack getControllerStack() {
        return this.controllerStack;
    }

    @Nullable
    public UUID getUUID() {
        var uuid = getWaystoneStorage().getUUID(this.position());

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

            if (!(data instanceof NetworkedWaystoneData networkedData)) return false;

            return networkedData.hasOwner();
        } else if (!this.controllerStack.isEmpty()) {
            return true;
        }

        return false;
    }

    public ActionResult attemptMossingInteraction(PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        var mossType = MossTypes.getMossType(stack);

        if (mossType != null && !this.mossTypeId.equals(mossType.getId())) {
            this.mossTypeId = mossType.getId();

            if (!world.isClient) {
                if (!this.mossStack.isEmpty()) spawnItemStackAbove(this.mossStack);

                world.playSound(null, getPos(), WAYSTONE_MOSS_APPLY, SoundCategory.BLOCKS, 1.0F, 1.0F);

                this.mossStack = stack.copyWithCount(1);

                ItemOps.decrementPlayerHandItem(player, hand);

                markDirty();
            }

            return ActionResult.SUCCESS;
        } else if (stack.isIn(ConventionalItemTags.SHEAR_TOOLS) && !this.mossTypeId.equals(MossTypes.EMPTY_ID)) {
            this.mossTypeId = MossTypes.EMPTY_ID;

            if (!world.isClient) {
                if (!this.mossStack.isEmpty()) spawnItemStackAbove(this.mossStack);

                world.playSound(null, getPos(), WAYSTONE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);

                this.mossStack = ItemStack.EMPTY;

                markDirty();
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
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
            getData();
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

    private static final double MAX_ADHD_DISTANCE = 12;
    private static final double MAX_FOCUS_DISTANCE = 4.5;

    public static void tickServer(World world, BlockPos pos, BlockState state, WaystoneBlockEntity waystone) {
        var controller = waystone.getControllerPos();
        --waystone.lookTime;

        if (world.getClosestPlayer(controller.x, controller.y, controller.z, MAX_FOCUS_DISTANCE, waystone::isValidFocus) instanceof PlayerEntity closestPlayer) {
            waystone.focusedEntity = closestPlayer;
        } else {
            if (waystone.focusedEntity != null && (!waystone.canSeeEntity(waystone.focusedEntity) || waystone.lookTime <= 0)) {
                waystone.focusedEntity = null;
            }
            if (waystone.focusedEntity == null && RANDOM.get().nextFloat() < 0.02) {
                var nearbyEntities = world.getOtherEntities(null, Box.from(controller).expand(MAX_FOCUS_DISTANCE), waystone::canSeeEntity);
                if (!nearbyEntities.isEmpty()) {
                    waystone.focusedEntity = nearbyEntities.get(RANDOM.get().nextInt(nearbyEntities.size()));
                    waystone.lookTime = 40 + RANDOM.get().nextInt(40);
                }
            }
            if (waystone.focusedEntity == null) {
                if (RANDOM.get().nextFloat() < 0.02) {
                    var offset = getRandomControllerOffset();
                    waystone.focusVector = waystone.focusVector != null ? waystone.focusVector.add(offset) : offset;
                    waystone.lookTime = 40 + RANDOM.get().nextInt(20);
                } else if (RANDOM.get().nextFloat() < 0.02) {
                    var storage = WaystoneDataStorage.getStorage(world);
                    var allWaystones = storage.getAllPositions().stream()
                        .filter(waystonePos -> !waystonePos.equals(waystone.position()))
                        .filter(waystonePos -> waystonePos.worldKey().equals(world.getRegistryKey()))
                        .toList();
                    if (!allWaystones.isEmpty()) {
                        var choice = allWaystones.stream().toList().get(RANDOM.get().nextInt(allWaystones.size()));
                        waystone.focusVector = choice.blockPos().toBottomCenterPos().add(waystone.getControllerPos()).subtract(controller).normalize();
                        waystone.lookTime = 40 + RANDOM.get().nextInt(20);
                    }
                }

            }
        }
        if (waystone.focusedEntity != null) waystone.focusVector = waystone.focusedEntity.getEyePos().subtract(controller).normalize();
        waystone.markDirty();
    }

    @Environment(EnvType.CLIENT)
    public static void tickClient(World world, BlockPos pos, BlockState state, WaystoneBlockEntity waystone) {
        var controller = waystone.getControllerPos();

        waystone.controllerRotation = new Quaternionf().lookAlong(waystone.focusVector.toVector3f(), new Vector3f(0, 1, 0)).invert();
        waystone.ticks++;

        var closestPlayer = world.getClosestPlayer(
            controller.x, controller.y, controller.z, 4.5,
            waystone::isValidFocus
        );

        if (closestPlayer != null) {
            waystone.shootRuneAt(closestPlayer);
            waystone.suckPortalParticleFrom(closestPlayer);
        }

        world.getOtherEntities(
            null,
            Box.from(controller).expand(6),
            waystone::canSeeEntity
        ).forEach(waystone::isValidFocus);

        waystone.suckARandomPortalParticle();
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean canSeeEntity(@Nullable Entity entity) {
        if (entity == null) return false;
        if (!entity.isAlive()) return false;
        if (entity.isSpectator()) return false;
        if (entity.isInvisible()) return false;
        if (world == null) return false;
        if (!entity.getWorld().equals(world)) return false;
        var controllerPos = getControllerPos();
        var entityPos = entity.getEyePos();
        if (controllerPos.distanceTo(entityPos) > MAX_ADHD_DISTANCE) return false;
        if (world.raycast(new RaycastContext(controllerPos, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ShapeContext.absent())).getType() != HitResult.Type.MISS) return false;
        return true;
    }

    private boolean isValidFocus(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return false;
        if (!canSeeEntity(player)) return false;
        if (getUUID() instanceof UUID uuid) return WaystonePlayerData.getData(player).hasDiscoverdWaystone(uuid);
        return false;
    }

    private void shootRuneAt(Entity target) {
        if (world == null || !this.isActive()) return;
        if (RANDOM.get().nextInt(10) != 0) return;
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
        if (RANDOM.get().nextInt(30) != 0) return;
        var watcherPos = this.getPos().toBottomCenterPos().add(0, getControllerHeight(), 0);
        var bb = target.getBoundingBox();
        var bbMin = bb.getMinPos();
        var bbMax = bb.getMaxPos();
        var end = new Vec3d(
            bbMin.x + (bbMax.x - bbMin.x) * RANDOM.get().nextDouble(),
            bbMin.y + (bbMax.y - bbMin.y) * RANDOM.get().nextDouble(),
            bbMin.z + (bbMax.z - bbMin.z) * RANDOM.get().nextDouble()
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
        if (RANDOM.get().nextInt(50) != 0) return;

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
        double theta = RANDOM.get().nextDouble() * 2 * Math.PI;
        double u = RANDOM.get().nextDouble();
        double phi = Math.acos(2 * u - 1);
        return new Vec3d(
            Math.sin(phi) * Math.cos(theta),
            Math.sin(phi) * Math.sin(theta),
            Math.cos(phi)
        );
    }

    private static Vec3d getRandomControllerOffset() {
        var rand = (Math.PI * 2) * RANDOM.get().nextDouble();
        return new Vec3d(Math.cos(rand), 0, Math.sin(rand));
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

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo(this.pos.toCenterPos()) <= 64.0D;
    }

    private WaystoneDataStorage getWaystoneStorage() {
        return WaystoneDataStorage.getStorage(this.world);
    }
}
