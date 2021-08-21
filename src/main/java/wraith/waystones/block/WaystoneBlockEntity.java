package wraith.waystones.block;


import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Utils;
import wraith.waystones.WaystoneValue;
import wraith.waystones.Waystones;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.registries.ItemRegistry;
import wraith.waystones.screens.WaystoneScreenHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class WaystoneBlockEntity extends LootableContainerBlockEntity implements SidedInventory, ExtendedScreenHandlerFactory, BlockEntityClientSerializable, WaystoneValue {

    private String name = "";
    private String hash;
    private boolean isGlobal = false;
    private UUID owner = null;
    private String ownerName = null;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);

    public WaystoneBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, pos, state);
        this.name = Utils.generateWaystoneName(this.name);
    }

    public void setOwner(PlayerEntity player) {
        if (player == null) {
            this.owner = null;
            this.ownerName = null;
        } else {
            this.owner = player.getUuid();
            this.ownerName = player.getName().asString();
        }
        markDirty();
    }

    public void createHash(World world, BlockPos pos) {
        this.hash = Utils.getSHA256("<POS X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", WORLD: \">" + world + "\">");
        markDirty();
    }

    @Override
    public WaystoneBlockEntity getEntity() {
        return this;
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
        return new WaystoneScreenHandler(syncId, this, player);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return createMenu(syncId, playerInventory, playerInventory.player);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText( "container." + Waystones.MOD_ID + ".waystone");
    }

    @Override
    protected Text getContainerName() {
        return getDisplayName();
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("waystone_name")) {
            this.name = tag.getString("waystone_name");
        }
        if (tag.contains("waystone_is_global")) {
            this.isGlobal = tag.getBoolean("waystone_is_global");
        }
        if (tag.contains("waystone_owner")) {
            this.owner = tag.getUuid("waystone_owner");
        }
        if (tag.contains("waystone_owner_name")) {
            this.ownerName = tag.getString("waystone_owner_name");
        }
        this.inventory = DefaultedList.ofSize(tag.getInt("inventory_size"), ItemStack.EMPTY);
        Inventories.readNbt(tag, inventory);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        return createTag(tag);
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
        tag.putInt("inventory_size", this.inventory.size());
        Inventories.writeNbt(tag, this.inventory);
        return tag;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void markDirty(){
        super.markDirty();
        if (world != null && !world.isClient) {
            sync();
        }
    }

    public DefaultedList<ItemStack> getInventory() {
        return inventory;
    }

    public void setInventory(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
        markDirty();
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        NbtCompound tag = createTag(new NbtCompound());
        tag.putString("waystone_hash", this.hash);
        packetByteBuf.writeNbt(tag);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
    public float lookingRotR = 0;
    private float turningSpeedR = 2;

    private long tickDelta = 0;


    private float rotClamp(int clampTo, float value)
    {
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
        return ((Rot - amount <= lookingRotR && lookingRotR <= Rot + amount)
                || (Rot2 - amount <= lookingRotR && lookingRotR <= Rot2 + amount));
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
        Random r = world.getRandom();
        Vec3d playerPos = player.getPos();
        ParticleEffect p = (r.nextInt(10) > 7) ? ParticleTypes.ENCHANT : ParticleTypes.PORTAL;

        int j = r.nextInt(2) * 2 - 1;
        int k = r.nextInt(2) * 2 - 1;

        double y = this.getPos().getY();

        int rd = r.nextInt(10);
        if(rd  > 5) {
            if(p == ParticleTypes.ENCHANT) {
                this.world.addParticle(p, playerPos.x, playerPos.y + 2D, playerPos.z,
                        (getPos().getX() + 0.5D - playerPos.x),
                        (y + -1.25D - playerPos.y),
                        (getPos().getZ() + 0.5D - playerPos.z));
            }
            else {
                this.world.addParticle(p, this.getPos().getX() + 0.5D, y + 0.8D, this.getPos().getZ() + 0.5D,
                        (playerPos.x - getPos().getX()) - r.nextDouble(),
                        (playerPos.y - getPos().getY()) - r.nextDouble() * 0.5D,
                        (playerPos.z - getPos().getZ()) - r.nextDouble());
            }
        }
        if(rd > 8) {
            this.world.addParticle(p, y + 0.5D, this.getPos().getY() + 0.8D,
                    this.getPos().getZ() + 0.5D, r.nextDouble() * j, (r.nextDouble() - 0.25D) * 0.125D,
                    r.nextDouble() * k);
        }
    }

    public void tick() {
        ++tickDelta;
        PlayerEntity closestPlayer = this.world.getClosestPlayer(this.getPos().getX() + 0.5D,
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

        if (tickDelta >= 360) {
            tickDelta = 0;
        }
        lookingRotR = rotClamp(360, lookingRotR);
    }

    @Override
    public String getWaystoneName() {
        return this.name;
    }

    @Override
    public String getWorldName() {
        return WaystoneBlock.getDimensionName(this.getWorld());
    }

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public String getDimension() {
        return WaystoneBlock.getDimensionName(world);
    }

    public void teleportPlayer(PlayerEntity player, boolean isAbyssWatcher) {
        if (!(player instanceof ServerPlayerEntity playerEntity)) {
            return;
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
        final List<StatusEffectInstance> effects = new ArrayList<>(playerEntity.getStatusEffects());
        if (playerEntity.getServer() == null) {
            return;
        }

        playerEntity.getServer().execute(() -> {
            player.getEntityWorld().sendEntityStatus(player, (byte) 46);
            playerEntity.teleport((ServerWorld) world, pos.getX() + fX, pos.getY(), pos.getZ() + fZ, fYaw, 0);
            playerEntity.onTeleportationDone();
            playerEntity.addExperience(0);
            if (isAbyssWatcher && playerEntity.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("abyss_watcher")) {
                if (!playerEntity.isCreative()) {
                    player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                    player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                    playerEntity.getMainHandStack().decrement(1);
                    player.world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1F, 1F);
                }
            }

            float absorption = playerEntity.getAbsorptionAmount();
            for (StatusEffectInstance effect : effects) {
                playerEntity.addStatusEffect(effect);
            }
            playerEntity.setAbsorptionAmount(absorption);
            player.getEntityWorld().sendEntityStatus(player, (byte) 46);
        });

    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public String getHash() {
        if (this.hash == null) {
            createHash(world, pos);
        }
        return this.hash;
    }

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

    public void setInventory(ArrayList<ItemStack> newInventory) {
        this.inventory = DefaultedList.ofSize(newInventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < newInventory.size(); ++i) {
            setItemInSlot(i, newInventory.get(i));
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

    public static <T extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, WaystoneBlockEntity waystone) {
        waystone.tick();
    }

}
