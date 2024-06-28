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

public record WaystonePacket(NbtCompound tag) implements CustomPayload{
    public static final Id<WaystonePacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_packet"));
    public static final PacketCodec<ByteBuf, NbtCompound> CODEC = PacketCodecs.NBT_COMPOUND.cast();

    public Id<WaystonePacket> getId() {
        return PACKET_ID;
    }

    public static ClientPlayNetworking.PlayPayloadHandler<WaystonePacket> getClientPlayHandler() {
        return (payload, context) -> {
            var nbt = payload.tag();
            var client = context.client();
            client.execute(() -> {
                if (FabricWaystones.WAYSTONE_STORAGE == null) {
                    FabricWaystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
                }
                FabricWaystones.WAYSTONE_STORAGE.fromTag(nbt);

                if (client.player == null) {
                    return;
                }
                HashSet<String> toForget = new HashSet<>();
                for (String hash : ((PlayerEntityMixinAccess) client.player).fabricWaystones$getDiscoveredWaystones()) {
                    if (!FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                        toForget.add(hash);
                    }
                }
                ((PlayerEntityMixinAccess) client.player).fabricWaystones$forgetWaystones(toForget);

                if (client.player.currentScreenHandler instanceof UniversalWaystoneScreenHandler) {
                    ((UniversalWaystoneScreenHandler) client.player.currentScreenHandler).updateWaystones(client.player);
                }
            });
        };
    }
}
