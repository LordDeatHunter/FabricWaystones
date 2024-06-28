package wraith.fwaystones.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record WaystoneDataPacket(String hash, String name, UUID owner, boolean isGlobal, boolean canUse, boolean isClient, String ownerName) implements CustomPayload {
    public static final Id<WaystoneDataPacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_packet"));
    public static final PacketCodec<RegistryByteBuf, WaystoneDataPacket> PACKET_CODEC = PacketCodec.of(WaystoneDataPacket::write, WaystoneDataPacket::new);

    public WaystoneDataPacket(RegistryByteBuf buf) {
        this(buf.readString(32767), buf.readString(32767), buf.readUuid(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readString(32767));
    }

    public WaystoneDataPacket(UUID owner, String hash, String name, boolean isGlobal, boolean canUse, boolean isClient, String ownerName) {
        this(hash, name, owner, isGlobal, canUse, isClient, ownerName);
    }

    public void write(RegistryByteBuf buf) {
        buf.writeString(hash);
        buf.writeString(name);
        buf.writeUuid(owner);
        buf.writeBoolean(isGlobal);
        buf.writeBoolean(canUse);
        buf.writeBoolean(isClient);
        buf.writeString(ownerName);
    }

    public static void write2(RegistryByteBuf buf, WaystoneDataPacket packet) {
        buf.writeString(packet.hash);
        buf.writeString(packet.name);
        buf.writeUuid(packet.owner);
        buf.writeBoolean(packet.isGlobal);
        buf.writeBoolean(packet.canUse);
        buf.writeBoolean(packet.isClient);
        buf.writeString(packet.ownerName);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return null;
    }
}
