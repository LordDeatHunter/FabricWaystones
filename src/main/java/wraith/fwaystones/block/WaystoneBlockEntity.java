package wraith.fwaystones.block;

import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.ComponentMap;
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
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.ExtendedStackReference;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.WaystoneAccess;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.item.components.WaystoneDataHolder;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.client.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.*;

import java.util.ArrayList;
import java.util.UUID;

public class WaystoneBlockEntity extends LootableContainerBlockEntity implements SidedInventory, ExtendedScreenHandlerFactory<WaystoneScreenOpenDataPacket>, WaystoneAccess {

    public float lookingRotR = 0;
    private float turningSpeedR = 2;

    private long tickDelta = 0;

    private WaystonePosition hash;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);

    @Nullable
    public WaystoneDataHolder dataHolder = null;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public WaystoneData getData() {
        return WaystoneDataStorage.getStorage(this.world).getData(this.position());
    }

    public boolean hasData() {
        return getData() != null;
    }

    public WaystonePosition position() {
        if (this.hash == null) createHash(world, pos);

        return this.hash;
    }

    @Nullable
    public UUID getUUID() {
        var uuid = WaystoneDataStorage.getStorage(this.world).getUUID(this.position());

        if (uuid == null) return WaystoneData.EMPTY_UUID;

        return uuid;
    }

    public void updateActiveState() {
        var data = this.getData();

        if (data == null) return;

        if (world != null && !world.isClient && world.getBlockState(pos).get(WaystoneBlock.ACTIVE) == (data.owner() == null)) {
            world.setBlockState(pos, world.getBlockState(pos).with(WaystoneBlock.ACTIVE, data.ownerName() != null));
            world.setBlockState(pos.up(), world.getBlockState(pos.up()).with(WaystoneBlock.ACTIVE, data.ownerName() != null));
        }
    }

    public void createHash(World world, BlockPos pos) {
        this.hash = new WaystonePosition(Utils.getDimensionName(world), pos);
        markDirty();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WaystoneBlockScreenHandler(syncId, playerInventory, this);
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
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
//        if (nbt.contains("waystone_name")) {
//            this.name = nbt.getString("waystone_name");
//        }
//        if (nbt.contains("waystone_is_global")) {
//            this.isGlobal = nbt.getBoolean("waystone_is_global");
//        }
//        if (nbt.contains("waystone_owner")) {
//            this.owner = nbt.getUuid("waystone_owner");
//        }
//        if (nbt.contains("waystone_owner_name")) {
//            this.ownerName = nbt.getString("waystone_owner_name");
//        }
//        this.color = nbt.contains("color", NbtElement.INT_TYPE) ? nbt.getInt("color") : null;
        this.inventory = DefaultedList.ofSize(nbt.getInt("inventory_size"), ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory, lookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        createTag(nbt);
    }

    private NbtCompound createTag(NbtCompound tag) {
        tag.putInt("inventory_size", this.inventory.size());
        Inventories.writeNbt(tag, this.inventory, world.getRegistryManager());
        return tag;
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);

        var storage = WaystoneDataStorage.getStorage(this.world);

        if (FabricWaystones.CONFIG.allowSavingWaystoneData()) {
            var holder = storage.removePositionAndExport(this);

            if (holder != null) {
                componentMapBuilder.add(WaystoneDataComponents.DATA_HOLDER, holder);
            }
        } else {
            storage.removePositionAndData(this);
        }
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);

        this.dataHolder = components.get(WaystoneDataComponents.DATA_HOLDER);

        if (this.dataHolder != null) {
            WaystoneDataStorage.getStorage(this.world).createGetOrImportData(this, dataHolder.data().color());

            this.updateActiveState();
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(world, pos);
        WorldOps.updateIfOnServer(world, pos.up());
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
        markDirty();
    }

    public void setInventory(ArrayList<ItemStack> newInventory) {
        this.inventory = DefaultedList.ofSize(newInventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < newInventory.size(); ++i) {
            setItemInSlot(i, newInventory.get(i));
        }
        markDirty();
    }

    private float rotClamp(int clampTo, float value) {
        if (value >= clampTo) {
            return value - clampTo;
        } else if (value < 0) {
            return value + clampTo;
        } else {
            return value;
        }
    }

    private boolean checkBound(int amount, float rot) {
        float Rot = Math.round(rot);
        float Rot2 = rotClamp(360, Rot + 180);
        return ((Rot - amount <= lookingRotR && lookingRotR <= Rot + amount) || (
            Rot2 - amount <= lookingRotR && lookingRotR <= Rot2 + amount));
    }

    private void moveOnTickR(float rot) {
        if (!checkBound(2, rot)) {
            double check = (rotClamp(180, rot) - rotClamp(180, lookingRotR) + 180) % 180;
            if (check < 90) {
                lookingRotR += turningSpeedR;
            } else {
                lookingRotR -= turningSpeedR;
            }
            lookingRotR = rotClamp(360, lookingRotR);
            if (checkBound(10, rot)) {
                turningSpeedR = 2;
            } else {
                turningSpeedR += 1;
                turningSpeedR = MathHelper.clamp(turningSpeedR, 2, 20);
            }
        }
    }

    private void addParticle(Entity target, boolean main) {
        if (world == null) return;
        var random = world.getRandom();
        ParticleEffect p = (random.nextInt(10) > 7) ? ParticleTypes.ENCHANT : ParticleTypes.PORTAL;
        var basePos = this.getPos().toBottomCenterPos();
        var watcherPos = basePos.add(0, 0.8, 0);
        var targetPos = target.getPos();

        int rd = random.nextInt(10);
        if (rd > 5) {
            if (p == ParticleTypes.ENCHANT) {
                var start = targetPos
                    .add(0, 1.25, 0);
                var distanceCheck = Double.compare(Math.abs(watcherPos.x - targetPos.x), Math.abs(watcherPos.z - targetPos.z));
                var end = basePos
                    .subtract(
                        distanceCheck > 0 ? Double.compare(watcherPos.x, targetPos.x) * 0.4 : 0,
                        0.05,
                        distanceCheck < 0 ? Double.compare(watcherPos.z, targetPos.z) * 0.4 : 0
                    )
                    .subtract(targetPos);
                this.world.addParticle(
                    p,
                    start.x, start.y, start.z,
                    end.x, end.y, end.z
                );
            } else if (main) {
                var start = watcherPos
                    .add(0, 0.95, 0);
//                velocity = eyePos
//                    .subtract(watcherPos)
//                    .subtract(randomDirection(random));
                var bb = target.getBoundingBox();
                var bbMin = bb.getMinPos();
                var bbMax = bb.getMaxPos();
                var endX = bbMin.x + (bbMax.x - bbMin.x) * random.nextDouble();
                var endY = bbMin.y + (bbMax.y - bbMin.y) * random.nextDouble();
                var endZ = bbMin.z + (bbMax.z - bbMin.z) * random.nextDouble();
                var end = new Vec3d(endX, endY, endZ)
                    .subtract(watcherPos.add(0, 1.8, 0));
//                    .add(randomDirection(random).multiply(0.005));
//                    .subtract(0, 1.25, 0);
                this.world.addParticle(
                    p,
                    start.x, start.y, start.z,
                    end.x, end.y, end.z
                );
                if (rd > 8) {
                    var randomDirection = randomDirection(random);
                    this.world.addParticle(
                        p,
                        watcherPos.x, watcherPos.y + 0.95, watcherPos.z,
                        randomDirection.x * 2,
                        randomDirection.y * 2 - 0.2,
                        randomDirection.z * 2
                    );
                }
            }
        }
    }

    public void tick() {
        if (world == null) return;

        if (world.isClient()) {
            ++tickDelta;
            if (getCachedState().get(WaystoneBlock.ACTIVE)) {
                var center = this.getPos().toBottomCenterPos().add(0, 1, 0);

                var closestPlayer = this.world.getClosestPlayer(
                    center.x, center.y, center.z,
                    4.5,
                    this::shouldWatchEntity
                );
                var others = this.world.getOtherEntities(
                    closestPlayer,
                    Box.of(center, 10, 10, 10),
                    this::shouldWatchEntity
                );
                if (closestPlayer != null) {
//                    for (int i = 0; i < 100; i++)
                    addParticle(closestPlayer, true);
                    double x = closestPlayer.getX() - this.getPos().getX() - 0.5D;
                    double z = closestPlayer.getZ() - this.getPos().getZ() - 0.5D;
                    float rotY = (float) ((float) Math.atan2(z, x) / Math.PI * 180 + 180);
                    moveOnTickR(rotY);
                } else {
                    lookingRotR += 2;
                }
                others.forEach(otherEntity -> addParticle(otherEntity, false));

                lookingRotR = rotClamp(360, lookingRotR);
            }

            if (tickDelta >= 360) {
                tickDelta = 0;
            }
        }
    }

    private boolean shouldWatchEntity(Entity entity) {
        if (entity == null) return false;
        if (!(entity instanceof PlayerEntity player)) return false;
        if (!EntityPredicates.EXCEPT_SPECTATOR.test(player)) return false;
        var data = WaystonePlayerData.getData(player);
        return data.hasDiscoverdWaystone(getUUID());
    }

    private static Vec3d randomDirection(Random random) {
        double theta = random.nextDouble() * 2 * Math.PI;
        double u = random.nextDouble();
        double phi = Math.acos(2 * u - 1);

        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);

        return new Vec3d(x, y, z);
    }

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo((double) this.pos.getX() + 0.5D,
                                        (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D
        ) <= 64.0D;
    }

    public boolean teleportPlayer(PlayerEntity player, boolean takeCost) {
        return teleportPlayer(player, takeCost, null);
    }

    public boolean teleportPlayer(PlayerEntity player, boolean takeCost, TeleportSources source) {
        if (!(player instanceof ServerPlayerEntity playerEntity)) return false;
        if (playerEntity.getServer() == null) return false;

        var facing = getCachedState().get(WaystoneBlock.FACING);

        float offsetX = 0;
        float offsetZ = 0;
        float yaw;

        if (facing.equals(Direction.UP) || facing.equals(Direction.DOWN)) {
            yaw = playerEntity.getYaw();
        } else {
            yaw = facing.getOpposite().asRotation();
        }

        if (facing == Direction.NORTH) {
            offsetX = 0.5f;
            offsetZ = -0.5f;
        } else if (facing == Direction.SOUTH) {
            offsetX = 0.5f;
            offsetZ = 1.5f;
        } else if (facing == Direction.EAST) {
            offsetX = 1.5f;
            offsetZ = 0.5f;
        } else if (facing == Direction.WEST) {
            offsetX = -0.5f;
            offsetZ = 0.5f;
        }

        TeleportTarget target = new TeleportTarget(
            (ServerWorld) getWorld(),
            new Vec3d(pos.getX() + offsetX, pos.getY(), pos.getZ() + offsetZ),
            new Vec3d(0, 0, 0),
            yaw,
            0,
            TeleportTarget.ADD_PORTAL_CHUNK_TICKET
        );

        if (source == null) return false;

        ExtendedStackReference stackReference = null;

        if (!playerEntity.isCreative() && source == TeleportSources.ABYSS_WATCHER) {
            stackReference = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(playerEntity, stack -> {
                var component = stack.get(WaystoneDataComponents.TELEPORTER);

                return component != null && component.oneTimeUse();
            });

            if (stackReference == null) return false;
        }

        var teleported = doTeleport(playerEntity, (ServerWorld) world, target, source, takeCost);

        if (!teleported) return false;

        if (!playerEntity.isCreative() && source == TeleportSources.ABYSS_WATCHER) {
            if (stackReference != null) {
                var stack = stackReference.get();
                var data = stack.get(WaystoneDataComponents.TELEPORTER);

                if (data != null && data.oneTimeUse()) {
                    stackReference.breakStack(stack.copy());

                    stack.decrement(1);

                    stackReference.set(stack);
                }
            }

        }

        return true;
    }

    private boolean doTeleport(ServerPlayerEntity player, ServerWorld world, TeleportTarget target, TeleportSources source, boolean takeCost) {
        var data = WaystonePlayerData.getData(player);
        var cooldown = data.teleportCooldown();

        if (source != TeleportSources.VOID_TOTEM && cooldown > 0) {
            var cooldownSeconds = Utils.df.format(cooldown / 20F);
            player.sendMessage(TextUtils.translationWithArg(
                    "no_teleport_message.cooldown",
                    cooldownSeconds
            ), false);
            return false;
        }

        if (!Utils.canTeleport(player, position(), source, takeCost)) {
            return false;
        }

        var cooldowns = FabricWaystones.CONFIG.teleportCooldowns;
        data.teleportCooldown(switch (source) {
            case WAYSTONE -> cooldowns.usedWaystone();
            case ABYSS_WATCHER -> cooldowns.usedAbyssWatcher();
            case LOCAL_VOID -> cooldowns.usedLocalVoid();
            case VOID_TOTEM -> cooldowns.usedVoidTotem();
            case POCKET_WORMHOLE -> cooldowns.usedPockedWormhole();
        });

        var oldPos = player.getBlockPos();
        if (!oldPos.isWithinDistance(target.pos(), 6) || !player.getWorld().getRegistryKey().equals(target.world().getRegistryKey())) {
            player.getWorld().playSound(null, oldPos, FabricWaystones.WAYSTONE_TELEPORT_PLAYER, SoundCategory.BLOCKS, 1F, 1F);
        }
        player.detach();
        player.teleportTo(target);
        BlockPos playerPos = player.getBlockPos();

        world.playSound(null, playerPos, FabricWaystones.WAYSTONE_TELEPORT_PLAYER, SoundCategory.BLOCKS, 1F, 1F);

        return true;
    }

    public void setOwner(PlayerEntity player) {
        var storage = WaystoneDataStorage.getStorage(world);
        var uuid = getUUID();

        if (player == null) {
            if (storage.setOwner(uuid, null)) {
                world.playSound(null, pos, FabricWaystones.WAYSTONE_DEATIVATE, SoundCategory.BLOCKS, 1F, 1F);
                world.playSound(null, pos, FabricWaystones.WAYSTONE_DEACTIVATE2, SoundCategory.BLOCKS, 1F, 1F);
            }
        } else {
            if (storage.setOwner(uuid, player)) {
                world.playSound(null, pos, FabricWaystones.WAYSTONE_INITIALIZE, SoundCategory.BLOCKS, 1F, 1F);
                world.playSound(null, pos, FabricWaystones.WAYSTONE_ACTIVATE, SoundCategory.BLOCKS, 1F, 1F);
            }
        }
        updateActiveState();
        markDirty();
    }

    public void setItemInSlot(int i, ItemStack itemStack) {
        this.inventory.set(i, itemStack);
    }

    public boolean hasStorage() {
        return !this.inventory.isEmpty();
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

    @Override
    public WaystoneScreenOpenDataPacket getScreenOpeningData(ServerPlayerEntity player) {
        return new WaystoneScreenOpenDataPacket(this.position(), this.canAccess(player));
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
