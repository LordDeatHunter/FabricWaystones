package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystonePlayerData;

public record SyncWaystonePlayerData(WaystonePlayerData data) {
    public static final StructEndec<SyncWaystonePlayerData> ENDEC = StructEndecBuilder.of(
            WaystonePlayerData.ENDEC.flatFieldOf(SyncWaystonePlayerData::data),
            SyncWaystonePlayerData::new
    );

    public static void handle(SyncWaystonePlayerData packet, PlayerEntity player) {
        WaystonePlayerData.setData(player, packet.data());
    }
}
