package wraith.fwaystones.block;

import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.Observable;
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
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.registry.DynamicRegistryManager;
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
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.item.components.WaystoneHashTarget;
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
    public Observable<@Nullable Entity> focusedEntity = Observable.of(null);

    public Vec3d focusVector = getRandomControllerOffset();

    private static final KeyedEndec<Vec3d> FOCUS_VECTOR_KEY = MinecraftEndecs.VEC3D.keyed("focusVector", () -> Vec3d.ZERO);

    //--

    public Entity teleportTarget = null;

    private WaystonePosition waystonePosition;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);

    @Nullable
    public WaystoneDataHolder dataHolder = null;

    private static final KeyedEndec<Identifier> WAYSTONE_TYPE_ID_KEY = MinecraftEndecs.IDENTIFIER.keyed("waystone_type", () -> WaystoneTypes.STONE);
    private Identifier waystoneTypeId = WaystoneTypes.STONE;

    private static final KeyedEndec<ItemStack> CONTROLLER_STACK_KEY = MinecraftEndecs.ITEM_STACK.keyed("controller_stack", () -> ItemStack.EMPTY);
    private ItemStack controllerStack = ItemStack.EMPTY;

    private static final KeyedEndec<Identifier> MOSS_TYPE_ID_KEY = MinecraftEndecs.IDENTIFIER.keyed("moss_type", () -> MossTypes.EMPTY_ID);
    private Identifier mossTypeId = MossTypes.EMPTY_ID;

    private static final KeyedEndec<ItemStack> MOSS_STACK_KEY = MinecraftEndecs.ITEM_STACK.keyed("moss_stack", () -> ItemStack.EMPTY);
    private ItemStack mossStack = ItemStack.EMPTY;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY, pos, state);

        focusedEntity.observe(entity -> {
            if (entity != null) {
                setFocusVector(entity.getEyePos().subtract(this.getControllerPos()).normalize());
            }
        });
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

        boolean setupData;

        if (this.controllerStack.getItem().equals(WaystoneItems.ABYSS_WATCHER)) {
            setupData = !(data instanceof NetworkedWaystoneData);
        } else {
            setupData = data == null;
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

    public UUID getUUID() {
        var uuid = getWaystoneStorage().getUUID(this.position());

        if (uuid == null) return WaystoneData.EMPTY_UUID;

        return uuid;
    }

    public WaystoneType getWaystoneType() {
        return WaystoneTypes.getTypeOrDefault(this.waystoneTypeId);
    }

    public TeleportAction createTeleportAction(TeleportSource source) {
        return TeleportAction.of(this.getUUID(), source);
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
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) lookup));

        this.inventory = DefaultedList.ofSize(tag.getInt("inventory_size"), ItemStack.EMPTY);
        Inventories.readNbt(tag, inventory, lookup);

        this.waystoneTypeId = tag.get(WAYSTONE_TYPE_ID_KEY);
        this.controllerStack = tag.get(ctx, CONTROLLER_STACK_KEY);

        this.mossTypeId = tag.get(MOSS_TYPE_ID_KEY);
        this.mossStack = tag.get(ctx, MOSS_STACK_KEY);

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
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) lookup));

        tag.putInt("inventory_size", this.inventory.size());
        Inventories.writeNbt(tag, this.inventory, lookup);

        tag.put(WAYSTONE_TYPE_ID_KEY, this.waystoneTypeId);
        tag.put(ctx, CONTROLLER_STACK_KEY, this.controllerStack);

        tag.put(MOSS_TYPE_ID_KEY, this.mossTypeId);
        tag.put(ctx, MOSS_STACK_KEY, this.mossStack);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
        var tag = super.toInitialChunkDataNbt(lookup);
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) lookup));

        tag.put(WAYSTONE_TYPE_ID_KEY, this.waystoneTypeId);
        tag.put(ctx, CONTROLLER_STACK_KEY, this.controllerStack);

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

    public boolean teleportEntity(Entity entity, TeleportSource source, boolean takeCost) {
        if (entity.getWorld().isClient) return false;

        return Utils.teleportPlayer(entity, createTeleportAction(source), takeCost);
    }

    private int teleportWaitCounter = 0;

    public void setupForTeleport(Entity entity) {
        if (entity.getWorld().isClient() || this.teleportTarget != null) return;

        this.teleportTarget = entity;
        this.teleportWaitCounter = 30;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(world, pos);
    }

    //--

    private static final double MAX_ADHD_DISTANCE = 12;
    private static final double MAX_FOCUS_DISTANCE = 4.5;

    public void tickServer(World world, BlockPos pos, BlockState state) {
        var controllerStack = this.controllerStack();
        if (controllerStack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEMS)) {
            if (this.teleportTarget != null) {


                var heightOffset = (isSingleBlock()) ? 1 : 2;

                var box = new Box(this.pos)
                    .offset(0, heightOffset / 2f, 0)
                    .expand(0, heightOffset, 0);

                if (!box.intersects(this.teleportTarget.getBoundingBox())) {
                    this.teleportTarget = null;
                } else {
                    if (this.teleportWaitCounter <= 0) {
                        var target = WaystoneHashTarget.get(controllerStack, world);
                        if (target != null) {
                            WaystoneComponentEventHooks.attemptTeleport(target, world, teleportTarget, controllerStack);
                        }
                        this.teleportWaitCounter = 0;
                    } else {
                        this.teleportWaitCounter--;
                    }
                }
            } else {

            }
        } else {
            this.teleportTarget = null;
        }


        //--

        var controller = this.getControllerPos();
        --this.lookTime;

        if (world.getClosestPlayer(controller.x, controller.y, controller.z, MAX_FOCUS_DISTANCE, this::isValidFocus) instanceof PlayerEntity closestPlayer) {
            this.focusedEntity.set(closestPlayer);
        } else {
            //noinspection DataFlowIssue
            // First check if the given focused entity can be seen or if look time for specific entity has run out
            if (this.focusedEntity.get() != null && (!this.canSeeEntity(this.focusedEntity.get()) || this.lookTime <= 0)) {
                this.focusedEntity.set(null);
            }
            // Second attempt to focus a new entity if the stars align
            if (this.focusedEntity.get() == null && RANDOM.get().nextFloat() < 0.02) {
                var nearbyEntities = world.getOtherEntities(null, Box.from(controller).expand(MAX_FOCUS_DISTANCE), this::canSeeEntity);
                if (!nearbyEntities.isEmpty()) {
                    this.focusedEntity.set(nearbyEntities.get(RANDOM.get().nextInt(nearbyEntities.size())));
                    this.lookTime = 40 + RANDOM.get().nextInt(40);
                }
            }
            // Third and finally look at something if the entity is found to be null
            if (this.focusedEntity.get() == null) {
                if (RANDOM.get().nextFloat() < 0.02) {
                    var offset = getRandomControllerOffset();
                    this.setFocusVector(this.focusVector != null ? this.focusVector.add(offset) : offset);
                    this.lookTime = 40 + RANDOM.get().nextInt(20);
                } else if (RANDOM.get().nextFloat() < 0.02) {
                    var storage = WaystoneDataStorage.getStorage(world);
                    var allWaystones = storage.getAllPositions().stream()
                        .filter(thisPos -> !thisPos.equals(this.position()))
                        .filter(thisPos -> thisPos.worldKey().equals(world.getRegistryKey()))
                        .toList();
                    if (!allWaystones.isEmpty()) {
                        var choice = allWaystones.stream().toList().get(RANDOM.get().nextInt(allWaystones.size()));
                        this.setFocusVector(choice.blockPos().toBottomCenterPos().add(this.getControllerPos()).subtract(controller).normalize());
                        this.lookTime = 40 + RANDOM.get().nextInt(20);
                    }
                }

            }
        }
    }

    @Environment(EnvType.CLIENT)
    public void tickClient(World world, BlockPos pos, BlockState state) {
        this.controllerRotation = new Quaternionf().lookAlong(this.focusVector.toVector3f(), new Vector3f(0, 1, 0)).invert();
        this.ticks++;

        if (!this.isActive()) return;

        var controller = this.getControllerPos();

        var closestPlayer = world.getClosestPlayer(
            controller.x, controller.y, controller.z, 4.5,
            this::isValidFocus
        );

        if (closestPlayer != null) {
            this.shootRuneAt(closestPlayer);
            this.suckPortalParticleFrom(closestPlayer);
        }

        world.getOtherEntities(
            null,
            Box.from(controller).expand(6),
            this::isValidFocus
        ).forEach(this::shootRuneAt);

        if (this.getData() != null) this.suckARandomPortalParticle();
    }

    private void setFocusVector(Vec3d vector) {
        if (this.focusVector.equals(vector)) return;

        this.focusVector = vector;

        this.markDirty();
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean canSeeEntity(Entity entity) {
        if (!entity.isAlive()) return false;
        if (entity.isSpectator()) return false;
        if (entity.isInvisible()) return false;
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

        return WaystonePlayerData.getData(player).hasDiscoverdWaystone(getUUID());
    }

    private void shootRuneAt(Entity target) {
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
        //noinspection DataFlowIssue
        this.world.addParticle(
            new RuneParticleEffect(getColor()),
            start.x, start.y, start.z,
            end.x, end.y, end.z
        );
    }

    private void suckPortalParticleFrom(Entity target) {
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
        //noinspection DataFlowIssue
        this.world.addParticle(
            ParticleTypes.PORTAL,
            watcherPos.x, watcherPos.y, watcherPos.z,
            end.x, end.y, end.z
        );
    }

    private void suckARandomPortalParticle() {
        if (RANDOM.get().nextInt(50) != 0) return;

        var controllerPos = getControllerPos();
        var randomDirection = getRandomDirection();
        //noinspection DataFlowIssue
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
        return (isSingleBlock() ? 16 : 29) / 16f;
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
