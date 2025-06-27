package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.UUID;

public record RevokeWaystoneOwner(UUID uuid) {
    public static final StructEndec<RevokeWaystoneOwner> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", RevokeWaystoneOwner::uuid),
            RevokeWaystoneOwner::new
    );

    public static void handle(RevokeWaystoneOwner packet, PlayerEntity player) {
        var uuid = packet.uuid();
        var storage = WaystoneDataStorage.getStorage(player);

        if (!storage.hasData(uuid)) return;

        var data = storage.getData(uuid);

        if (data == null) return;

        if (player.getUuid().equals(data.ownerID()) || player.hasPermissionLevel(2)) {
            storage.setOwner(uuid, null);
        }
    }
}
