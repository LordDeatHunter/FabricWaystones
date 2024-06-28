package wraith.fwaystones.packets.client;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

public record VoidRevivePacket(NbtCompound tag) implements CustomPayload{
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "void_totem_revive"));
    private static final VoidRevivePacket INSTANCE = new VoidRevivePacket(new NbtCompound());
    public static final Codec<VoidRevivePacket> CODEC = Codec.unit(INSTANCE);
    public static final PacketCodec PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public Id getId() {
        return PACKET_ID;
    }
}
