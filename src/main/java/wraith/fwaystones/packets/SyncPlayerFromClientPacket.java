package wraith.fwaystones.packets;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public record SyncPlayerFromClientPacket(NbtCompound tag) implements CustomPayload{
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "sync_player_from_client"));
    public static final PacketCodec CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND,
            SyncPlayerFromClientPacket::tag,
            SyncPlayerFromClientPacket::new
    );
    public Id getId() {
        return PACKET_ID;
    }
}
