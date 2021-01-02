package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Utils;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;

public class UniversalWaystoneScreenHandler extends ScreenHandler {

    protected UniversalWaystoneScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        int waystoneID = Math.floorDiv(id, 2);
        Waystone waystone = Waystones.WAYSTONE_DATABASE.getWaystoneFromClick(player, waystoneID);

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        if (waystone == null) {
            return false;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("id", waystone.name);
        data.writeCompoundTag(tag);
        if (id % 2 != 0) {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
                return false;
            }
            ClientPlayNetworking.send(Utils.ID("forget_waystone"), data);
            return true;
        }
        else if (Utils.canTeleport(player, null)) {
            data = new PacketByteBuf(Unpooled.buffer());
            tag = new CompoundTag();
            tag.putString("WorldName", waystone.world);
            tag.putString("Facing", waystone.facing);
            tag.putIntArray("Coordinates", new int[]{waystone.pos.getX(), waystone.pos.getY(), waystone.pos.getZ()});
            data.writeCompoundTag(tag);
            if (player instanceof ServerPlayerEntity) {
                Waystones.teleportPlayer(player, tag);
            }
            else {
                ClientPlayNetworking.send(Utils.ID("teleport_player"), data);
            }
            return true;
        } return false;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

}
