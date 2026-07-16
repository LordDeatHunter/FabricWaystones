package wraith.fwaystones.packets;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record SyncPlayerFromClientPacket(CompoundTag tag) implements CustomPacketPayload{
    public static final Type PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, "sync_player_from_client"));
    public static final StreamCodec CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncPlayerFromClientPacket::tag,
            SyncPlayerFromClientPacket::new
    );
    public Type type() {
        return PACKET_ID;
    }
}
