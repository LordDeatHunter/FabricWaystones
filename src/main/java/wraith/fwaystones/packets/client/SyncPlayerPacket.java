package wraith.fwaystones.packets.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import wraith.fwaystones.FabricWaystones;

public record SyncPlayerPacket(CompoundTag tag) implements CustomPacketPayload{
    public static final Type PACKET_ID = new Type<>(Identifier.fromNamespaceAndPath(FabricWaystones.MOD_ID, "sync_player"));
    public static final StreamCodec CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncPlayerPacket::tag,
            SyncPlayerPacket::new
    );

    public Type type() {
        return PACKET_ID;
    }
}
