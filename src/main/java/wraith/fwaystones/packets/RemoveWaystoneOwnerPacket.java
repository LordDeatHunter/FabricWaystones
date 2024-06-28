package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record RemoveWaystoneOwnerPacket(UUID owner, String waystone) implements CustomPayload {
    public static final CustomPayload.Id<RemoveWaystoneOwnerPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "remove_waystone_owner"));
    public static final Codec<RemoveWaystoneOwnerPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(RemoveWaystoneOwnerPacket::owner),
            Codec.STRING.fieldOf("waystoneHash").forGetter(RemoveWaystoneOwnerPacket::waystone)
    ).apply(instance, RemoveWaystoneOwnerPacket::new));

    public CustomPayload.Id<RemoveWaystoneOwnerPacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<RemoveWaystoneOwnerPacket> getServerPlayHandler() {
        return (payload, context) -> context.server().execute(() -> {
            String hash = payload.waystone();
            UUID owner = payload.owner();
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(hash)) {
                return;
            }
            if ((context.player().getUuid().equals(owner) || context.player().hasPermissionLevel(2))) {
                FabricWaystones.WAYSTONE_STORAGE.setOwner(hash, null);
            }
        });
    }
}