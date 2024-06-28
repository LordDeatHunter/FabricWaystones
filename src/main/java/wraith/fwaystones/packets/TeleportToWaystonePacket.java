package wraith.fwaystones.packets;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

public record TeleportToWaystonePacket(String waystone) implements CustomPayload {
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "teleport_to_waystone"));
    public static final PacketCodec CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            TeleportToWaystonePacket::waystone,
            TeleportToWaystonePacket::new
    );
    public Id getId() {
        return PACKET_ID;
    }
}
