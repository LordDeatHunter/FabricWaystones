package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record RequestPlayerSyncPacket() implements CustomPacketPayload{
    public static final Type PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, "request_player_waystone_update"));
    private static final RequestPlayerSyncPacket INSTANCE = new RequestPlayerSyncPacket();
    public static final Codec<RequestPlayerSyncPacket> CODEC = Codec.unit(RequestPlayerSyncPacket.INSTANCE);
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Type type() {
        return PACKET_ID;
    }
}
