package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import wraith.waystones.Utils;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.CustomScreenHandlerRegistry;

import java.util.UUID;
import java.util.function.Function;

public class WaystoneScreenHandler extends UniversalWaystoneScreenHandler {

    private String name;
    private String hash;
    private UUID owner;
    private boolean isGlobal;
    private Function<PlayerEntity, Boolean> canUse = null;
    private boolean isConfig = false;
    private boolean isClient;
    private String ownerName;

    public WaystoneScreenHandler(int syncId, WaystoneBlockEntity waystoneEntity, PlayerEntity player) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, player);
        this.hash = waystoneEntity.getHash();
        this.name = waystoneEntity.getName();
        this.owner = waystoneEntity.getOwner();
        this.isGlobal = waystoneEntity.isGlobal();
        this.canUse = waystoneEntity::canAccess;
        this.isClient = player.world.isClient;
        this.ownerName = waystoneEntity.getOwnerName();
    }

    public WaystoneScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId, playerInventory.player);
        this.isClient = playerInventory.player.world.isClient;
        CompoundTag tag = buf.readCompoundTag();
        if (tag != null) {
            this.hash = tag.getString("waystone_hash");
            this.name = tag.getString("waystone_name");
            this.owner = tag.getUuid("waystone_owner");
            this.ownerName = tag.getString("waystone_owner_name");
            this.isGlobal = tag.getBoolean("waystone_is_global");
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse != null ? canUse.apply(player) : true;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (isConfig) {
            //TODO
            return true;
        } else {
            return super.onButtonClick(player, id);
        }
    }

    public String getWaystone() {
        return this.hash;
    }

    public void toggleGlobal() {
        if (!isClient) {
            return;
        }
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeString(this.hash);
        ClientPlayNetworking.send(Utils.ID("toggle_global_waystone"), packet);
    }

    public boolean isOwner(PlayerEntity player) {
        return this.owner.equals(player.getUuid());
    }

    public String getName() {
        return this.name;
    }

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

}
