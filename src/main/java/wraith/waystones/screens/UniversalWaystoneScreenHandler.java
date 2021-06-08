package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.waystones.PlayerAccess;
import wraith.waystones.Utils;

import java.util.ArrayList;

public class UniversalWaystoneScreenHandler extends ScreenHandler {

    protected UniversalWaystoneScreenHandler(ScreenHandlerType<? extends UniversalWaystoneScreenHandler> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!player.world.isClient) {
            return false;
        }

        int waystoneID = Math.floorDiv(id, 2);
        ArrayList<String> waystones = ((PlayerAccess)player).getHashesSorted();
        if (waystoneID >= waystones.size()) {
            return false;
        }

        String waystone = waystones.get(waystoneID);
        if (waystone == null) {
            return false;
        }

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        CompoundTag tag = new CompoundTag();
        tag.putString("waystone_hash", waystone);
        data.writeCompoundTag(tag);

        if (id % 2 != 0) {
            ClientPlayNetworking.send(Utils.ID("forget_waystone"), data);
        }
        else {
            ClientPlayNetworking.send(Utils.ID("teleport_to_waystone"), data);
        }
        return true;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

}
