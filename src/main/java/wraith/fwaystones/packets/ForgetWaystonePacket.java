package wraith.fwaystones.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;

public record ForgetWaystonePacket(String waystoneHash) implements CustomPayload {
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "forget_waystone"));
    public static final PacketCodec CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            ForgetWaystonePacket::waystoneHash,
            ForgetWaystonePacket::new
    );

    public Id getId() {
        return PACKET_ID;
    }
}
