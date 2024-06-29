package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.TeleportSources;

public record TeleportToWaystonePacket(String waystone, String source) implements CustomPayload {
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "teleport_to_waystone"));
    public static final Codec<TeleportToWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StringIdentifiable.EnumCodec.STRING.fieldOf("source").forGetter(TeleportToWaystonePacket::source),
            Codec.STRING.fieldOf("waystone").forGetter(TeleportToWaystonePacket::waystone)
    ).apply(instance, TeleportToWaystonePacket::new));
    public static final PacketCodec PACKET_CODEC = PacketCodecs.registryCodec(CODEC);
    public Id getId() {
        return PACKET_ID;
    }

    public TeleportSources getSource() {
        return TeleportSources.valueOf(source);
    }
}
