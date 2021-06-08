package wraith.waystones.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.CustomScreenHandlerRegistry;

import java.util.function.Function;

public class WaystoneScreenHandler extends UniversalWaystoneScreenHandler {

    private String hash;
    private Function<PlayerEntity, Boolean> canUse = null;

    public WaystoneScreenHandler(int syncId, WaystoneBlockEntity waystoneEntity) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        this.hash = waystoneEntity.getHash();
        canUse = waystoneEntity::canAccess;
    }

    public WaystoneScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        if (buf != null) {
            CompoundTag tag = buf.readCompoundTag();
            if (tag != null) {
                if (tag.contains("waystone_hash")) {
                    this.hash = tag.getString("waystone_hash");
                }
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse != null ? canUse.apply(player) : true;
    }

    public String getWaystone() {
        return this.hash;
    }

}
