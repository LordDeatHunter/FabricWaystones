package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.TeleportSources;

public record TeleportToWaystonePacket(String waystone, String source) implements CustomPacketPayload {
    public static final Type PACKET_ID = new Type<>(Identifier.fromNamespaceAndPath(FabricWaystones.MOD_ID, "teleport_to_waystone"));
    public static final Codec<TeleportToWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("waystone").forGetter(TeleportToWaystonePacket::waystone),
            StringRepresentable.EnumCodec.STRING.fieldOf("source").forGetter(TeleportToWaystonePacket::source)
    ).apply(instance, TeleportToWaystonePacket::new));
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public Type type() {
        return PACKET_ID;
    }

    public TeleportSources getSource() {
        return TeleportSources.valueOf(source);
    }
}
