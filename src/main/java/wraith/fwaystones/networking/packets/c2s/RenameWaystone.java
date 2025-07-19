package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.NetworkedWaystoneData;

import java.util.UUID;

public record RenameWaystone(UUID uuid, String name) {
    public static final StructEndec<RenameWaystone> ENDEC = StructEndecBuilder.of(
        BuiltInEndecs.UUID.fieldOf("uuid", RenameWaystone::uuid),
        Endec.STRING.fieldOf("name", RenameWaystone::name),
        RenameWaystone::new
    );

    public static void handle(RenameWaystone packet, PlayerEntity player) {
        var uuid = packet.uuid();
        var storage = WaystoneDataStorage.getStorage(player);

        if (!storage.hasData(uuid)) return;

        var data = storage.getData(uuid);

        if (!(data instanceof NetworkedWaystoneData networkedData)) return;

        if (player.getUuid().equals(networkedData.ownerID()) || player.hasPermissionLevel(2)) {
            storage.renameWaystone(uuid, packet.name());
        }
    }
}
