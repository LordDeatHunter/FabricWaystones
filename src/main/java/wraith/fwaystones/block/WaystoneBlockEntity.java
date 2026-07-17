package wraith.fwaystones.block;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
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

public class WaystoneBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer,
    ExtendedMenuProvider<WaystoneDataPacket>, WaystoneValue {

    public float lookingRotR = 0;
    private String name = "";
    private String hash;
    private boolean isGlobal = false;
    private UUID owner = null;
    private String ownerName = null;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(0, ItemStack.EMPTY);
    private Integer color;
    private float turningSpeedR = 2;
    private long tickDelta = 0;

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, pos, state);
        this.name = Utils.generateWaystoneName(this.name);
    }

    public static void ticker(Level world, BlockPos blockPos, BlockState blockState, WaystoneBlockEntity waystone) {
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
        if (level != null && !level.isClientSide() && level.getBlockState(worldPosition).getValue(WaystoneBlock.ACTIVE) == (owner == null)) {
            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(WaystoneBlock.ACTIVE, this.ownerName != null));
            level.setBlockAndUpdate(worldPosition.above(), level.getBlockState(worldPosition.above()).setValue(WaystoneBlock.ACTIVE, this.ownerName != null));
        }
    }

    public void createHash(Level world, BlockPos pos) {
        this.hash = createHashString(Utils.getDimensionName(world), pos);
        setChanged();
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
        setChanged();
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new WaystoneBlockScreenHandler(syncId, this, player);
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return createMenu(syncId, playerInventory, playerInventory.player);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + FabricWaystones.MOD_ID + ".waystone");
    }

    @Override
    protected Component getDefaultName() {
        return getDisplayName();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        nbt.getString("waystone_name").ifPresent(name -> this.name = name);
        this.isGlobal = nbt.getBooleanOr("waystone_is_global", this.isGlobal);
        nbt.read("waystone_owner", UUIDUtil.CODEC).ifPresent(owner -> this.owner = owner);
        nbt.getString("waystone_owner_name").ifPresent(ownerName -> this.ownerName = ownerName);
        this.color = nbt.getInt("color").orElse(null);
        this.inventory = NonNullList.withSize(nbt.getIntOr("inventory_size", 0), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        createTag(nbt);
    }

    private void createTag(ValueOutput tag) {
        tag.putString("waystone_name", this.name);
        if (this.owner != null) {
            tag.store("waystone_owner", UUIDUtil.CODEC, this.owner);
        }
        if (this.ownerName != null) {
            tag.putString("waystone_owner_name", this.ownerName);
        }
        tag.putBoolean("waystone_is_global", this.isGlobal);
        if (this.color != null) {
            tag.putInt("color", this.color);
        }
        tag.putInt("inventory_size", this.inventory.size());
        ContainerHelper.saveAllItems(tag, this.inventory);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && level instanceof ServerLevel serverWorld) {
            serverWorld.getChunkSource().blockChanged(worldPosition);
        }
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
        setChanged();
    }

    public void setInventory(ArrayList<ItemStack> newInventory) {
        this.inventory = NonNullList.withSize(newInventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < newInventory.size(); ++i) {
            setItemInSlot(i, newInventory.get(i));
        }
        setChanged();
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
                turningSpeedR = Mth.clamp(turningSpeedR, 2, 20);
            }
        }
    }

    private void addParticle(Player player) {
        if (level == null) {
            return;
        }
        var r = level.getRandom();
        Vec3 playerPos = player.position();
        ParticleOptions p = (r.nextInt(10) > 7) ? ParticleTypes.ENCHANT : ParticleTypes.PORTAL;

        int j = r.nextInt(2) * 2 - 1;
        int k = r.nextInt(2) * 2 - 1;

        double y = this.getBlockPos().getY() + 1;

        int rd = r.nextInt(10);
        if (rd > 5) {
            if (p == ParticleTypes.ENCHANT) {
                this.level.addParticle(p, playerPos.x, playerPos.y + 1.5D, playerPos.z,
                    (getBlockPos().getX() + 0.5D - playerPos.x), (y - 1.25D - playerPos.y),
                    (getBlockPos().getZ() + 0.5D - playerPos.z));
            } else {
                this.level.addParticle(p, this.getBlockPos().getX() + 0.5D, y + 0.8D,
                    this.getBlockPos().getZ() + 0.5D,
                    (playerPos.x - getBlockPos().getX()) - r.nextDouble(),
                    (playerPos.y - getBlockPos().getY() - 0.5D) - r.nextDouble() * 0.5D,
                    (playerPos.z - getBlockPos().getZ()) - r.nextDouble());
            }
        }
        if (rd > 8) {
            this.level.addParticle(p, y + 0.5D, this.getBlockPos().getY() + 0.8D,
                this.getBlockPos().getZ() + 0.5D,
                r.nextDouble() * j, (r.nextDouble() - 0.25D) * 0.125D, r.nextDouble() * k);
        }
    }

    public void tick() {
        if (level == null) {
            return;
        }
        ++tickDelta;
        if (getBlockState().getValue(WaystoneBlock.ACTIVE)) {
            var closestPlayer = this.level.getNearestPlayer(this.getBlockPos().getX() + 0.5D,
                this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ() + 0.5D, 4.5, false);
            if (closestPlayer != null) {
                addParticle(closestPlayer);
                double x = closestPlayer.getX() - this.getBlockPos().getX() - 0.5D;
                double z = closestPlayer.getZ() - this.getBlockPos().getZ() - 0.5D;
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
        return this.getBlockPos();
    }

    @Override
    public String getWorldName() {
        return level == null ? "" : Utils.getDimensionName(level);
    }

    public boolean canAccess(Player player) {
        return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D,
            (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public boolean teleportPlayer(Player player, boolean takeCost) {
        return teleportPlayer(player, takeCost, null);
    }

    public boolean teleportPlayer(Player player, boolean takeCost, TeleportSources source) {
        if (!(player instanceof ServerPlayer playerEntity)) {
            return false;
        }
        Direction facing = getBlockState().getValue(WaystoneBlock.FACING);
        float x = 0;
        float z = 0;
        float yaw = playerEntity.getYRot();
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
        if (playerEntity.level().getServer() == null) {
            return false;
        }
        TeleportTransition target = new TeleportTransition(
            (ServerLevel) getLevel(),
            new Vec3(worldPosition.getX() + fX, worldPosition.getY(), worldPosition.getZ() + fZ),
            new Vec3(0, 0, 0),
            fYaw,
            0,
            TeleportTransition.PLACE_PORTAL_TICKET
        );
        if (source == null) {
            return false;
        }
        var teleported = doTeleport(playerEntity, (ServerLevel) level, target, source, takeCost);
        if (!teleported) {
            return false;
        }
        if (!playerEntity.isCreative() && source == TeleportSources.ABYSS_WATCHER) {
            for (var hand : InteractionHand.values()) {
                Item handItem = playerEntity.getItemInHand(hand).getItem();
                if (handItem instanceof AbyssWatcherItem) {
                    player.onEquippedItemBroken(handItem, hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    playerEntity.getItemInHand(hand).shrink(1);
                    player.level().playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1F, 1F);
                    break;
                }
            }
        }
        return true;
    }

    private boolean doTeleport(ServerPlayer player, ServerLevel world, TeleportTransition target, TeleportSources source, boolean takeCost) {
        var playerAccess = (PlayerEntityMixinAccess) player;
        var cooldown = playerAccess.fabricWaystones$getTeleportCooldown();
        if (source != TeleportSources.VOID_TOTEM && cooldown > 0) {
            var cooldownSeconds = Utils.df.format(cooldown / 20F);
            player.sendSystemMessage(Component.translatable(
                "fwaystones.no_teleport_message.cooldown",
                Component.literal(cooldownSeconds).withStyle(style ->
                    style.withColor(TextColor.parseColor(Component.translatable(
                        "fwaystones.no_teleport_message.cooldown.arg_color").getString()).getOrThrow())
                )
            ));
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
        var oldPos = player.blockPosition();
        player.level().playSound(null, oldPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
        player.unRide();
        player.teleport(target);
        BlockPos playerPos = player.blockPosition();

        if (!oldPos.closerThan(playerPos, 6) || !player.level().dimension().equals(world.dimension())) {
            world.playSound(null, playerPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
        }
        return true;
    }

    public void setName(String name) {
        this.name = name;
        setChanged();
    }

    @Override
    public String getHash() {
        if (this.hash == null) {
            createHash(level, worldPosition);
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
        setChanged();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(Player player) {
        if (player == null) {
            if (this.owner != null && this.level != null) {
                level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1F, 1F);
                level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 1F, 1F);
            }
            this.owner = null;
            this.ownerName = null;
        } else {
            if (this.owner == null && level != null) {
                level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1F, 1F);
                level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.BLOCKS, 1F, 1F);
            }
            this.owner = player.getUUID();
            this.ownerName = player.getName().getString();
        }
        updateActiveState();
        setChanged();
    }

    public void toggleGlobal() {
        this.isGlobal = !this.isGlobal;
        setChanged();
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
    public int[] getSlotsForFace(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public WaystoneDataPacket getScreenOpeningData(ServerPlayer player) {
        return new WaystoneDataPacket(this.hash, this.name, this.owner, this.isGlobal, this.canAccess(player), player.level().isClientSide(), this.ownerName);
    }

}
