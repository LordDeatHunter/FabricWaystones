package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
    private Function<Player, Boolean> canUse = null;
    private String ownerName = "";

    public WaystoneBlockScreenHandler(int syncId, WaystoneBlockEntity waystoneEntity, Player player) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, player);
        this.hash = waystoneEntity.getHash();
        this.name = waystoneEntity.getWaystoneName();
        this.owner = waystoneEntity.getOwner();
        this.isGlobal = waystoneEntity.isGlobal();
        this.canUse = waystoneEntity::canAccess;
        this.isClient = player.level().isClientSide();
        this.ownerName = waystoneEntity.getOwnerName();
        updateWaystones(player);
    }

    public WaystoneBlockScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, playerInventory.player);
        this.isClient = playerInventory.player.level().isClientSide();
        CompoundTag tag = buf.readNbt();
        if (tag != null) {
            this.hash = tag.getStringOr("waystone_hash", "");
            this.name = tag.getStringOr("waystone_name", "");
            this.owner = tag.read("waystone_owner", UUIDUtil.CODEC).orElse(null);
            tag.getString("waystone_owner_name").ifPresent(ownerName -> this.ownerName = ownerName);
            this.isGlobal = tag.getBooleanOr("waystone_is_global", false);
        }
        updateWaystones(player);
    }

    public WaystoneBlockScreenHandler(int syncId, Inventory playerInventory, WaystoneDataPacket waystoneDataPacket) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, playerInventory.player);
        this.isClient = playerInventory.player.level().isClientSide();
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
    public void updateWaystones(Player player) {
        super.updateWaystones(player);
        if (!player.level().isClientSide()) {
            return;
        }
        if (!FabricWaystones.WAYSTONE_STORAGE.containsHash(this.hash)) {
            closeScreen();
        }
        if (!this.sortedWaystones.contains(this.hash)) {
            this.sortedWaystones.add(this.hash);
            this.filterWaystones();
        }
    }

    @Override
    public boolean stillValid(Player player) {
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

    public boolean isOwner(Player player) {
        return this.owner != null && this.owner.equals(player.getUUID());
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
