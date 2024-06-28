package wraith.fwaystones.packets.client;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.util.WaystoneStorage;

import java.util.HashSet;

public record SyncPlayerPacket(NbtCompound tag) implements CustomPayload{
    public static final Id<SyncPlayerPacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "sync_player"));
    public static final PacketCodec<ByteBuf, NbtCompound> CODEC = PacketCodecs.NBT_COMPOUND.cast();

    public Id<SyncPlayerPacket> getId() {
        return PACKET_ID;
    }

    public static ClientPlayNetworking.PlayPayloadHandler<SyncPlayerPacket> getClientPlayHandler() {
        return (payload, context) -> {
            var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    ((PlayerEntityMixinAccess) client.player).fabricWaystones$fromTagW(payload.tag());
                }
            });
        };
    }
}
