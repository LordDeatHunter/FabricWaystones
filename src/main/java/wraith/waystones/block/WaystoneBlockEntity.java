package wraith.waystones.block;


import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import wraith.waystones.Utils;
import wraith.waystones.Waystones;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.registries.ItemRegistry;
import wraith.waystones.screens.WaystoneScreenHandler;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class WaystoneBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private String name = "";
    private String hash;
    private boolean isGlobal = false;
    private UUID owner = null;
    private String ownerName;

    public WaystoneBlockEntity() {
        super(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY);
        this.name = Utils.generateWaystoneName(this.name);
    }

    public void setOnwer(PlayerEntity player) {
        this.owner = player.getUuid();
        this.ownerName = player.getName().asString();
        markDirty();
    }

    public void createHash(World world, BlockPos pos) {
        this.hash = Utils.getSHA256("<POS X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", WORLD: \">" + world + "\">");
        markDirty();
    }

    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WaystoneScreenHandler(syncId, this, player);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText( "container." + Waystones.MOD_ID + ".waystone");
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        if (tag.contains("waystone_name")) {
            this.name = tag.getString("waystone_name");
        }
        if (tag.contains("waystone_hash")) {
            this.hash = tag.getString("waystone_hash");
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
        Inventories.fromTag(tag, inventory);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        return createTag(tag);
    }

    private CompoundTag createTag(CompoundTag tag) {
        tag.putInt("inventory_size", this.inventory.size());
        tag.putString("waystone_name", this.name);
        if (this.hash == null) {
            createHash(this.world, this.pos);
        }
        tag.putString("waystone_hash", this.hash);
        if (this.owner != null) {
            tag.putUuid("waystone_owner", this.owner);
        }
        if (this.ownerName != null) {
            tag.putString("waystone_owner_name", this.ownerName);
        }
        tag.putBoolean("waystone_is_global", this.isGlobal);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

    @Override
    public void markDirty(){
        super.markDirty();
        if (world != null && !world.isClient) {
            sync();
        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeCompoundTag(createTag(new CompoundTag()));
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(world.getBlockState(pos), tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return toTag(tag);
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

    Random r = new Random();

    private void addParticle() {
        int j = r.nextInt(2) * 2 - 1;
        int k = r.nextInt(2) * 2 - 1;
        ParticleEffect p;
        if (r.nextInt(10) > 7) {
            p = ParticleTypes.ENCHANT;
        } else {
            p = ParticleTypes.PORTAL;
        }
        this.world.addParticle(p, this.getPos().getX() + 0.5D + (j * 0.25D), this.getPos().getY() + r.nextDouble(),
                this.getPos().getZ() + 0.5D + (k * 0.25D), r.nextDouble() * j, (r.nextDouble() - 0.5D) * 0.125D,
                r.nextDouble() * k);
    }


    @Override
    public void tick() {
        ++tickDelta;
        PlayerEntity closestPlayer = this.world.getClosestPlayer(this.getPos().getX() + 0.5D,
                this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D, 4.5, false);
        if (closestPlayer != null) {
            addParticle();
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

    public String getName() {
        return this.name;
    }

    public boolean canAccess(PlayerEntity player) {
        return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public String getDimension() {
        return WaystoneBlock.getDimensionName(world);
    }

    public void teleportPlayer(PlayerEntity player, boolean isAbyssWatcher) {
        if (world.isClient) {
            return;
        }
        ServerPlayerEntity playerEntity = (ServerPlayerEntity) player;
        Direction facing = getCachedState().get(WaystoneBlock.FACING);
        float x = 0;
        float z = 0;
        float yaw = playerEntity.yaw;
        switch (facing) {
            case NORTH:
                x = 0.5f;
                z = -0.5f;
                yaw = 0;
                break;
            case SOUTH:
                x = 0.5f;
                z = 1.5f;
                yaw = 180;
                break;
            case EAST:
                x = 1.5f;
                z = 0.5f;
                yaw = 90;
                break;
            case WEST:
                x = -0.5f;
                z = 0.5f;
                yaw = 270;
                break;
        }

        final float fX = x;
        final float fZ = z;
        final float fYaw = yaw;
        final List<StatusEffectInstance> effects = playerEntity.getStatusEffects().stream().map(StatusEffectInstance::new).collect(Collectors.toList());

        playerEntity.getServer().execute(() -> {
            playerEntity.teleport((ServerWorld) world, pos.getX() + fX, pos.getY(), pos.getZ() + fZ, fYaw, 0);
            playerEntity.onTeleportationDone();
            playerEntity.addExperience(0);
            playerEntity.clearStatusEffects();
            if (!playerEntity.isCreative() && isAbyssWatcher && playerEntity.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("abyss_watcher")) {
                playerEntity.getMainHandStack().decrement(1);
                SoundEvent se = SoundEvents.ENTITY_ENDER_EYE_DEATH;
                playerEntity.playSound(se, 1.0F, 1.0F);
            }

            for (StatusEffectInstance effect : effects) {
                playerEntity.addStatusEffect(effect);
            }
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

}
