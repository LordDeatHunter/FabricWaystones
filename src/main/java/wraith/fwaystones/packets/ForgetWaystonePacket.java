package wraith.fwaystones.packets;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;

public record ForgetWaystonePacket(String waystoneHash) implements CustomPacketPayload {
    public static final Type PACKET_ID = new Type<>(Identifier.fromNamespaceAndPath(FabricWaystones.MOD_ID, "forget_waystone"));
    public static final StreamCodec CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ForgetWaystonePacket::waystoneHash,
            ForgetWaystonePacket::new
    );

    public Type type() {
        return PACKET_ID;
    }
}
