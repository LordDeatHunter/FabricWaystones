package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.block.WaystoneDataPacket;
import wraith.fwaystones.packets.ToggleGlobalWaystonePacket;
import wraith.fwaystones.registry.CustomScreenHandlerRegistry;

import java.util.UUID;
import java.util.function.Function;

public class WaystoneBlockScreenHandler extends UniversalWaystoneScreenHandler {

    private final boolean isClient;
    private String name;
    private String hash;
    private UUID owner;
    private boolean isGlobal;
    private Function<PlayerEntity, Boolean> canUse = null;
    private String ownerName = "";

    public WaystoneBlockScreenHandler(int syncId, WaystoneBlockEntity waystoneEntity, PlayerEntity player) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, player);
        this.hash = waystoneEntity.getHash();
        this.name = waystoneEntity.getWaystoneName();
        this.owner = waystoneEntity.getOwner();
        this.isGlobal = waystoneEntity.isGlobal();
        this.canUse = waystoneEntity::canAccess;
        this.isClient = player.getWorld().isClient;
        this.ownerName = waystoneEntity.getOwnerName();
        updateWaystones(player);
    }

    public WaystoneBlockScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, playerInventory.player);
        this.isClient = playerInventory.player.getWorld().isClient;
        NbtCompound tag = buf.readNbt();
        if (tag != null) {
            this.hash = tag.getString("waystone_hash");
            this.name = tag.getString("waystone_name");
            if (tag.contains("waystone_owner")) {
                this.owner = tag.getUuid("waystone_owner");
            }
            if (tag.contains("waystone_owner_name")) {
                this.ownerName = tag.getString("waystone_owner_name");
            }
            this.isGlobal = tag.getBoolean("waystone_is_global");
        }
        updateWaystones(player);
    }

    public WaystoneBlockScreenHandler(int syncId, PlayerInventory playerInventory, WaystoneDataPacket waystoneDataPacket) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, playerInventory.player);
        this.isClient = playerInventory.player.getWorld().isClient;
        this.hash = waystoneDataPacket.hash();
        this.name = waystoneDataPacket.name();
        this.owner = waystoneDataPacket.owner();
        this.isGlobal = waystoneDataPacket.isGlobal();
        this.canUse = player -> waystoneDataPacket.canUse();
        this.ownerName = waystoneDataPacket.ownerName();
        updateWaystones(player);
    }

    @Override
    public void onForget(String waystone) {
        if (this.hash.equals(waystone)) {
            closeScreen();
        }
    }

    @Override
    public void updateWaystones(PlayerEntity player) {
        super.updateWaystones(player);
        if (!player.getWorld().isClient) {
            return;
        }
        if (!FabricWaystones.WAYSTONE_STORAGE.containsHash(this.hash)) {
            closeScreen();
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse != null ? canUse.apply(player) : true;
    }

    public String getWaystone() {
        return this.hash;
    }

    public void toggleGlobal() {
        if (!isClient) {
            return;
        }
        ClientPlayNetworking.send(new ToggleGlobalWaystonePacket(this.owner, this.hash));
        this.isGlobal = !this.isGlobal;
    }

    public boolean isOwner(PlayerEntity player) {
        return this.owner != null && this.owner.equals(player.getUuid());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName == null ? "" : this.ownerName;
    }

    public void removeOwner() {
        this.owner = null;
        this.ownerName = null;
    }

    public boolean hasOwner() {
        return this.owner != null;
    }

}
