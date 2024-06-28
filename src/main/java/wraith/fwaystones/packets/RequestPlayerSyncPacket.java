package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record RequestPlayerSyncPacket() implements CustomPayload{
    public static final CustomPayload.Id<RequestPlayerSyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "request_player_waystone_update"));
    private static final RequestPlayerSyncPacket INSTANCE = new RequestPlayerSyncPacket();
    public static final Codec<RequestPlayerSyncPacket> CODEC = Codec.unit(RequestPlayerSyncPacket.INSTANCE);

    public CustomPayload.Id<RequestPlayerSyncPacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<RequestPlayerSyncPacket> getServerPlayHandler() {
        return (payload, context) -> {
            context.server().execute(((PlayerEntityMixinAccess) context.player())::fabricWaystones$syncData);
        };
    }
}
