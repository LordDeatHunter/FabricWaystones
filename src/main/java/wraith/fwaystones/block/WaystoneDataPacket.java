package wraith.fwaystones.block;

import wraith.fwaystones.FabricWaystones;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WaystoneDataPacket(String hash, String name, UUID owner, boolean isGlobal, boolean canUse, boolean isClient, String ownerName) implements CustomPacketPayload {
    public static final Type<WaystoneDataPacket> PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, "waystone_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneDataPacket> PACKET_CODEC = StreamCodec.ofMember(WaystoneDataPacket::write, WaystoneDataPacket::new);

    public WaystoneDataPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(32767), buf.readUtf(32767), buf.readUUID(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readUtf(32767));
    }

    public WaystoneDataPacket(UUID owner, String hash, String name, boolean isGlobal, boolean canUse, boolean isClient, String ownerName) {
        this(hash, name, owner, isGlobal, canUse, isClient, ownerName);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(hash);
        buf.writeUtf(name);
        buf.writeUUID(owner);
        buf.writeBoolean(isGlobal);
        buf.writeBoolean(canUse);
        buf.writeBoolean(isClient);
        buf.writeUtf(ownerName);
    }

    public static void write2(RegistryFriendlyByteBuf buf, WaystoneDataPacket packet) {
        buf.writeUtf(packet.hash);
        buf.writeUtf(packet.name);
        buf.writeUUID(packet.owner);
        buf.writeBoolean(packet.isGlobal);
        buf.writeBoolean(packet.canUse);
        buf.writeBoolean(packet.isClient);
        buf.writeUtf(packet.ownerName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
