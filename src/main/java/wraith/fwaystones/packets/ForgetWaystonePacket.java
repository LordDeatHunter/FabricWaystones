package wraith.fwaystones.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;

public record ForgetWaystonePacket(String waystoneHash) implements CustomPayload {
    public static final CustomPayload.Id<ForgetWaystonePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "teleport_to_waystone"));
    public static final PacketCodec<ByteBuf, String> PACKET_CODEC = PacketCodecs.STRING;

    public CustomPayload.Id<ForgetWaystonePacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<ForgetWaystonePacket> getPlayPayloadHandler() {
        return (payload, context) -> context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystoneHash())) {
                return;
            }

            var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystoneHash());
            if (waystone.getWorld() != null && !(waystone.getWorld().getBlockState(waystone.getPos()).getBlock() instanceof WaystoneBlock)) {
                FabricWaystones.WAYSTONE_STORAGE.removeWaystone(payload.waystoneHash());
                waystone.getWorld().removeBlockEntity(waystone.getPos());
            } else {
                waystone.teleportPlayer(context.player(), true);
            }
        });
    }
}
