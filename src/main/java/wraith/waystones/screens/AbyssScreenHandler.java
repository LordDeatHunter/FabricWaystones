package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import wraith.waystones.PlayerAccess;
import wraith.waystones.Utils;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

public class AbyssScreenHandler extends UniversalWaystoneScreenHandler {

    public AbyssScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, syncId);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!player.world.isClient) {
            return false;
        }
        int waystoneID = Math.floorDiv(id, 2);
        String waystone = ((PlayerAccess)player).getHashesSorted().get(waystoneID);
        if (waystone == null) {
            return false;
        }

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        CompoundTag tag = new CompoundTag();
        tag.putString("waystone_hash", waystone);
        tag.putBoolean("from_abyss_watcher", true);
        data.writeCompoundTag(tag);

        if (id % 2 != 0) {
            ClientPlayNetworking.send(Utils.ID("forget_waystone"), data);
        }
        else if (player.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("abyss_watcher")) {
            ClientPlayNetworking.send(Utils.ID("teleport_to_waystone"), data);
            return true;
        }
        return true;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("abyss_watcher");
    }

}
