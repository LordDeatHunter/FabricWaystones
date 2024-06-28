package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record RequestPlayerSyncPacket() implements CustomPayload{
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "request_player_waystone_update"));
    private static final RequestPlayerSyncPacket INSTANCE = new RequestPlayerSyncPacket();
    public static final Codec<RequestPlayerSyncPacket> CODEC = Codec.unit(RequestPlayerSyncPacket.INSTANCE);
    public static final PacketCodec PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public Id getId() {
        return PACKET_ID;
    }
}
