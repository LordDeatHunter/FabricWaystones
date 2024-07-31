package wraith.fwaystones.packets.client;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

public record WaystonePacket(NbtCompound tag) implements CustomPayload{
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_packet"));
    public static final PacketCodec CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND,
            WaystonePacket::tag,
            WaystonePacket::new
    );

    public Id getId() {
        return PACKET_ID;
    }
}
