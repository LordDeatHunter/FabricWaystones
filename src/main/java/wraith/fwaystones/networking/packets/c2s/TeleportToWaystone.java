package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.UUID;

public record TeleportToWaystone(UUID uuid, TeleportSource source) {
    public static final StructEndec<TeleportToWaystone> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", TeleportToWaystone::uuid),
            TeleportSource.ENDEC.fieldOf("sources", TeleportToWaystone::source),
            TeleportToWaystone::new
    );

    public static void handle(TeleportToWaystone packet, PlayerEntity player) {
        var storage = WaystoneDataStorage.getStorage(player);
        var uuid = packet.uuid();

        if (!storage.hasData(uuid)) return;

        if (!WaystonePlayerData.getData(player).discoveredWaystones().contains(packet.uuid()) && !storage.isGlobal(packet.uuid())) {
            return;
        }

        var waystone = storage.getEntity(uuid);

        if (waystone != null) {
            waystone.teleportEntity(player, packet.source(), true);
        }
    }
}
