package wraith.fwaystones.block;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.access.WaystoneValue;
import wraith.fwaystones.item.AbyssWatcherItem;
import wraith.fwaystones.registry.BlockEntityRegistry;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.TeleportSources;
import wraith.fwaystones.util.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class WaystoneBlockEntity extends LootableContainerBlockEntity implements SidedInventory,
    ExtendedScreenHandlerFactory, WaystoneValue {

    public float lookingRotR = 0;
    private String name = "";
    private String hash;
    private boolean isGlobal = false;
    private UUID owner = null;
    private String ownerName = null;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);
    private Integer color;
    private float turningSpeedR = 2;
    private long tickDelta = 0;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, pos, state);
        this.name = Utils.generateWaystoneName(this.name);
    }

    public static void ticker(World world, BlockPos blockPos, BlockState blockState, WaystoneBlockEntity waystone) {
        waystone.tick();
    }

    public static String createHashString(String dimensionName, BlockPos pos) {
        return Utils.getSHA256(
                "<POS X:" + pos.getX() +
                        ", Y:" + pos.getY() +
                        ", Z:" + pos.getZ() +
                        ", WORLD: \">" + dimensionName + "\">"
        );
    }

    public void updateActiveState() {
        if (world != null && !world.isClient && world.getBlockState(pos).get(WaystoneBlock.ACTIVE) == (owner == null)) {
            world.setBlockState(pos, world.getBlockState(pos).with(WaystoneBlock.ACTIVE, this.ownerName != null));
            world.setBlockState(pos.up(), world.getBlockState(pos.up()).with(WaystoneBlock.ACTIVE, this.ownerName != null));
        }
    }

    public void createHash(World world, BlockPos pos) {
        this.hash = createHashString(Utils.getDimensionName(world), pos);
        markDirty();
    }

    @Override
    public WaystoneBlockEntity getEntity() {
        return this;
    }

    @Override
    public int getColor() {
        if (this.color == null) this.color = Utils.getRandomColor();
        return this.color;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
        markDirty();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WaystoneBlockScreenHandler(syncId, this, player);
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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("waystone_name")) {
            this.name = nbt.getString("waystone_name");
        }
        if (nbt.contains("waystone_is_global")) {
            this.isGlobal = nbt.getBoolean("waystone_is_global");
        }
        if (nbt.contains("waystone_owner")) {
            this.owner = nbt.getUuid("waystone_owner");
        }
        if (nbt.contains("waystone_owner_name")) {
            this.ownerName = nbt.getString("waystone_owner_name");
        }
        this.color = nbt.contains("color", NbtElement.INT_TYPE) ? nbt.getInt("color") : null;
        this.inventory = DefaultedList.ofSize(nbt.getInt("inventory_size"), ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        createTag(nbt);
    }

    private NbtCompound createTag(NbtCompound tag) {
        tag.putString("waystone_name", this.name);
        if (this.owner != null) {
            tag.putUuid("waystone_owner", this.owner);
        }
        if (this.ownerName != null) {
            tag.putString("waystone_owner_name", this.ownerName);
        }
        tag.putBoolean("waystone_is_global", this.isGlobal);
        if (this.color != null) {
            tag.putInt("color", this.color);
        }
        tag.putInt("inventory_size", this.inventory.size());
        Inventories.writeNbt(tag, this.inventory);
        return tag;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
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

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity,
                                       PacketByteBuf packetByteBuf) {
        NbtCompound tag = createTag(new NbtCompound());
        tag.putString("waystone_hash", this.hash);
        packetByteBuf.writeNbt(tag);
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

    private void addParticle(PlayerEntity player) {
        if (world == null) {
            return;
        }
        var r = world.getRandom();
        Vec3d playerPos = player.getPos();
        ParticleEffect p = (r.nextInt(10) > 7) ? ParticleTypes.ENCHANT : ParticleTypes.PORTAL;

        int j = r.nextInt(2) * 2 - 1;
        int k = r.nextInt(2) * 2 - 1;

        double y = this.getPos().getY() + 1;

        int rd = r.nextInt(10);
        if (rd > 5) {
            if (p == ParticleTypes.ENCHANT) {
                this.world.addParticle(p, playerPos.x, playerPos.y + 1.5D, playerPos.z,
                    (getPos().getX() + 0.5D - playerPos.x), (y - 1.25D - playerPos.y),
                    (getPos().getZ() + 0.5D - playerPos.z));
            } else {
                this.world.addParticle(p, this.getPos().getX() + 0.5D, y + 0.8D,
                    this.getPos().getZ() + 0.5D,
                    (playerPos.x - getPos().getX()) - r.nextDouble(),
                    (playerPos.y - getPos().getY() - 0.5D) - r.nextDouble() * 0.5D,
                    (playerPos.z - getPos().getZ()) - r.nextDouble());
            }
        }
        if (rd > 8) {
            this.world.addParticle(p, y + 0.5D, this.getPos().getY() + 0.8D,
                this.getPos().getZ() + 0.5D,
                r.nextDouble() * j, (r.nextDouble() - 0.25D) * 0.125D, r.nextDouble() * k);
        }
    }

    public void tick() {
        if (world == null) {
            return;
        }
        ++tickDelta;
        if (getCachedState().get(WaystoneBlock.ACTIVE)) {
            var closestPlayer = this.world.getClosestPlayer(this.getPos().getX() + 0.5D,
                this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D, 4.5, false);
            if (closestPlayer != null) {
                addParticle(closestPlayer);
                double x = closestPlayer.getX() - this.getPos().getX() - 0.5D;
                double z = closestPlayer.getZ() - this.getPos().getZ() - 0.5D;
                float rotY = (float) ((float) Math.atan2(z, x) / Math.PI * 180 + 180);
                moveOnTickR(rotY);
            } else {
                lookingRotR += 2;
            }

            lookingRotR = rotClamp(360, lookingRotR);
        }

        if (tickDelta >= 360) {
            tickDelta = 0;
        }
    }

    @Override
    public String getWaystoneName() {
        return this.name;
    }

    @Override
    public BlockPos way_getPos() {
        return this.getPos();
    }

    @Override
    public String getWorldName() {
        return world == null ? "" : Utils.getDimensionName(world);
    }

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo((double) this.pos.getX() + 0.5D,
            (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public boolean teleportPlayer(PlayerEntity player, boolean takeCost) {
        return teleportPlayer(player, takeCost, null);
    }

    public boolean teleportPlayer(PlayerEntity player, boolean takeCost, TeleportSources source) {
        if (!(player instanceof ServerPlayerEntity playerEntity)) {
            return false;
        }
        Direction facing = getCachedState().get(WaystoneBlock.FACING);
        float x = 0;
        float z = 0;
        float yaw = playerEntity.getYaw();
        switch (facing) {
            case NORTH -> {
                x = 0.5f;
                z = -0.5f;
                yaw = 0;
            }
            case SOUTH -> {
                x = 0.5f;
                z = 1.5f;
                yaw = 180;
            }
            case EAST -> {
                x = 1.5f;
                z = 0.5f;
                yaw = 90;
            }
            case WEST -> {
                x = -0.5f;
                z = 0.5f;
                yaw = 270;
            }
        }
        final float fX = x;
        final float fZ = z;
        final float fYaw = yaw;
        if (playerEntity.getServer() == null) {
            return false;
        }
        TeleportTarget target = new TeleportTarget(
            new Vec3d(pos.getX() + fX, pos.getY(), pos.getZ() + fZ),
            new Vec3d(0, 0, 0),
            fYaw,
            0
        );
        if (source == null) {
            source = Utils.getTeleportSource(playerEntity);
        }
        if (source == null) {
            return false;
        }
        var teleported = doTeleport(playerEntity, (ServerWorld) world, target, source, takeCost);
        if (!teleported) {
            return false;
        }
        if (!playerEntity.isCreative() && source == TeleportSources.ABYSS_WATCHER) {
            for (var hand : Hand.values()) {
                if (playerEntity.getStackInHand(hand).getItem() instanceof AbyssWatcherItem) {
                    player.sendToolBreakStatus(hand);
                    playerEntity.getStackInHand(hand).decrement(1);
                    player.getWorld().playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1F, 1F);
                    break;
                }
            }
        }
        return true;
    }

    private boolean doTeleport(ServerPlayerEntity player, ServerWorld world, TeleportTarget target, TeleportSources source, boolean takeCost) {
        var playerAccess = (PlayerEntityMixinAccess) player;
        var cooldown = playerAccess.fabricWaystones$getTeleportCooldown();
        if (source != TeleportSources.VOID_TOTEM && cooldown > 0) {
            var cooldownSeconds = Utils.df.format(cooldown / 20F);
            player.sendMessage(Text.translatable(
                "fwaystones.no_teleport_message.cooldown",
                Text.literal(cooldownSeconds).styled(style ->
                    style.withColor(TextColor.parse(Text.translatable(
                        "fwaystones.no_teleport_message.cooldown.arg_color").getString()))
                )
            ), false);
            return false;
        }
        if (!Utils.canTeleport(player, hash, source, takeCost)) {
            return false;
        }
        var cooldowns = FabricWaystones.CONFIG.teleportation_cooldown;
        playerAccess.fabricWaystones$setTeleportCooldown(switch (source) {
            case WAYSTONE -> cooldowns.cooldown_ticks_from_waystone();
            case ABYSS_WATCHER -> cooldowns.cooldown_ticks_from_abyss_watcher();
            case LOCAL_VOID -> cooldowns.cooldown_ticks_from_local_void();
            case VOID_TOTEM -> cooldowns.cooldown_ticks_from_void_totem();
            case POCKET_WORMHOLE -> cooldowns.cooldown_ticks_from_pocket_wormhole();
        });
        var oldPos = player.getBlockPos();
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, new ChunkPos(BlockPos.ofFloored(target.position)), 1, player.getId());
        player.getWorld().playSound(null, oldPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
        player.detach();
        FabricDimensions.teleport(player, world, target);
        BlockPos playerPos = player.getBlockPos();

        if (!oldPos.isWithinDistance(playerPos, 6) || !player.getWorld().getRegistryKey().equals(world.getRegistryKey())) {
            world.playSound(null, playerPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
        }
        return true;
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    @Override
    public String getHash() {
        if (this.hash == null) {
            createHash(world, pos);
        }
        return this.hash;
    }

    public byte[] getHashByteArray() {
        var hash = getHash();
        var values = hash.substring(1, hash.length() - 1).split(", ");
        var bytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) {
            bytes[i] = Byte.parseByte(values[i]);
        }
        return bytes;
    }

    public String getHexHash() {
        BigInteger number = new BigInteger(1, getHashByteArray());
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    @Override
    public boolean isGlobal() {
        return this.isGlobal;
    }

    public void setGlobal(boolean global) {
        this.isGlobal = global;
        markDirty();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(PlayerEntity player) {
        if (player == null) {
            if (this.owner != null && this.world != null) {
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 1F, 1F);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1F, 1F);
            }
            this.owner = null;
            this.ownerName = null;
        } else {
            if (this.owner == null && world != null) {
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1F, 1F);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT, SoundCategory.BLOCKS, 1F, 1F);
            }
            this.owner = player.getUuid();
            this.ownerName = player.getName().getString();
        }
        updateActiveState();
        markDirty();
    }

    public void toggleGlobal() {
        this.isGlobal = !this.isGlobal;
        markDirty();
    }

    public String getOwnerName() {
        return this.ownerName;
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

}
