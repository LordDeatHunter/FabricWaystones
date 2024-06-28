package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record RenameWaystonePacket(UUID owner, String waystone, String name) implements CustomPayload {
    public static final CustomPayload.Id<RenameWaystonePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_gui_slot_click"));
    public static final Codec<RenameWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("owner").forGetter(RenameWaystonePacket::owner),
            Codec.STRING.fieldOf("waystone").forGetter(RenameWaystonePacket::waystone),
            Codec.STRING.fieldOf("name").forGetter(RenameWaystonePacket::name)
    ).apply(instance, RenameWaystonePacket::new));

    public CustomPayload.Id<RenameWaystonePacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<RenameWaystonePacket> getServerPlayHandler() {
        return (payload, context) -> context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(payload.waystone()) &&
                    ((context.player().getUuid().equals(payload.owner()) &&
                            payload.owner().equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystone()).getOwner())) ||
                            context.player().hasPermissionLevel(2))) {
                FabricWaystones.WAYSTONE_STORAGE.renameWaystone(payload.waystone(), payload.name());
            }
        });
    }
}
