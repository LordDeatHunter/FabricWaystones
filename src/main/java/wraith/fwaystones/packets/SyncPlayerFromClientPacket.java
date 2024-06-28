package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

// TODO: Make this packet sync the player data from the client.
public record SyncPlayerFromClientPacket() implements CustomPayload{
    public static final Id<SyncPlayerFromClientPacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "sync_player_from_client"));
    private static final SyncPlayerFromClientPacket INSTANCE = new SyncPlayerFromClientPacket();
    public static final Codec<SyncPlayerFromClientPacket> CODEC = Codec.unit(SyncPlayerFromClientPacket.INSTANCE);

    public CustomPayload.Id<SyncPlayerFromClientPacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<SyncPlayerFromClientPacket> getPlayPayloadHandler() {
        return (payload, context) -> {
            context.server().execute(() -> ((PlayerEntityMixinAccess) context.player()).fabricWaystones$syncData());
        };
    }
}
