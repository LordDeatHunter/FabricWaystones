package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.NetworkedWaystoneData;

import java.util.UUID;

import static wraith.fwaystones.util.FWConfigModel.PermissionLevel;

public record ToggleGlobalWaystone(UUID uuid) {
    public static final StructEndec<ToggleGlobalWaystone> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", ToggleGlobalWaystone::uuid),
            ToggleGlobalWaystone::new
    );

    public static void handle(ToggleGlobalWaystone packet, PlayerEntity player) {
        var uuid = packet.uuid();
        var storage = WaystoneDataStorage.getStorage(player);

        if (!storage.hasData(uuid)) return;

        var level = FabricWaystones.CONFIG.globalTogglePermission();

        if (level.equals(PermissionLevel.NONE)) return;
        if (level.equals(PermissionLevel.OP) && !player.hasPermissionLevel(2)) return;
        if (level.equals(PermissionLevel.OWNER)) {
            if (storage.getData(uuid) instanceof NetworkedWaystoneData networkedData && !player.getUuid().equals(networkedData.ownerID())) {
                return;
            }
        }

        if (storage.hasData(uuid)) storage.toggleGlobal(uuid);
    }
}
