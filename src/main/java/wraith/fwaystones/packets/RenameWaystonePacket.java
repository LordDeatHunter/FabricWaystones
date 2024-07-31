package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record RenameWaystonePacket(UUID owner, String waystone, String name) implements CustomPayload {
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "rename_waystone"));
    public static final Codec<RenameWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(RenameWaystonePacket::owner),
            Codec.STRING.fieldOf("waystone").forGetter(RenameWaystonePacket::waystone),
            Codec.STRING.fieldOf("name").forGetter(RenameWaystonePacket::name)
    ).apply(instance, RenameWaystonePacket::new));
    public static final PacketCodec PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public Id getId() {
        return PACKET_ID;
    }
}
