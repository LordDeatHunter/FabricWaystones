package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record ToggleGlobalWaystonePacket(UUID owner, String waystone) implements CustomPayload {
    public static final CustomPayload.Id<ToggleGlobalWaystonePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "toggle_global_waystone"));
    public static final Codec<ToggleGlobalWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(ToggleGlobalWaystonePacket::owner),
            Codec.STRING.fieldOf("waystoneHash").forGetter(ToggleGlobalWaystonePacket::waystone)
    ).apply(instance, ToggleGlobalWaystonePacket::new));

    public CustomPayload.Id<ToggleGlobalWaystonePacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<ToggleGlobalWaystonePacket> getPlayPayloadHandler() {
        return (payload, context) -> context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }

            var permissionLevel = FabricWaystones.CONFIG.global_mode_toggle_permission_levels();
            switch (permissionLevel) {
                case NONE:
                    return;
                case OP:
                    if (!player.hasPermissionLevel(2)) {
                        return;
                    }
                    break;
                case OWNER:
                    if (!context.player().getUuid().equals(payload.owner()) || !payload.owner().equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystone()).getOwner())) {
                        return;
                    }
                    break;
            }
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(payload.waystone())) {
                FabricWaystones.WAYSTONE_STORAGE.toggleGlobal(payload.waystone());
            }
        });
    }
}
