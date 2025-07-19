package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.util.Utils;

import java.util.UUID;

public record ToggleGlobalWaystone(UUID uuid) {
    public static final StructEndec<ToggleGlobalWaystone> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", ToggleGlobalWaystone::uuid),
            ToggleGlobalWaystone::new
    );

    public static void handle(ToggleGlobalWaystone packet, PlayerEntity player) {
        var uuid = packet.uuid();
        var storage = WaystoneDataStorage.getStorage(player);

        if (!storage.hasData(uuid)) return;
        if (!Utils.hasPermission(player, uuid, FabricWaystones.CONFIG::globalTogglePermission)) return;

        storage.toggleGlobal(uuid);
    }
}
