package wraith.fwaystones.packets.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import wraith.fwaystones.FabricWaystones;

public record VoidRevivePacket() implements CustomPacketPayload{
    public static final Type PACKET_ID = new Type<>(Identifier.fromNamespaceAndPath(FabricWaystones.MOD_ID, "void_totem_revive"));
    private static final VoidRevivePacket INSTANCE = new VoidRevivePacket();
    public static final Codec<VoidRevivePacket> CODEC = MapCodec.unitCodec(VoidRevivePacket.INSTANCE);
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Type type() {
        return PACKET_ID;
    }
}
