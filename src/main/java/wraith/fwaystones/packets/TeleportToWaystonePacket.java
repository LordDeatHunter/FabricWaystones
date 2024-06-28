package wraith.fwaystones.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record TeleportToWaystonePacket(String waystone) implements CustomPayload {
    public static final Id<TeleportToWaystonePacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "forget_waystone"));
    public static final PacketCodec<ByteBuf, String> PACKET_CODEC = PacketCodecs.STRING;

    public CustomPayload.Id<TeleportToWaystonePacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<TeleportToWaystonePacket> getPlayPayloadHandler() {
        return (payload, context) -> context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }

            context.server().execute(() -> ((PlayerEntityMixinAccess) context.player()).fabricWaystones$forgetWaystone(payload.waystone()));
        });
    }
}
