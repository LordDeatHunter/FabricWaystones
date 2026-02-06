package wraith.fwaystones.networking.packets;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.WaystonePlayerDataKey;

public record SyncWaystonePlayerDataChange(WaystonePlayerDataKey<?> key, Object data) {
    public static final StructEndec<SyncWaystonePlayerDataChange> ENDEC = (StructEndec<SyncWaystonePlayerDataChange>) Endec.dispatchedStruct(key -> {
        return StructEndecBuilder.of(
                WaystonePlayerDataKey.ENDEC.fieldOf("name", SyncWaystonePlayerDataChange::key),
                ((Endec<Object>) key.endec()).fieldOf("data", SyncWaystonePlayerDataChange::data),
                SyncWaystonePlayerDataChange::new
        );
    }, SyncWaystonePlayerDataChange::key, WaystonePlayerDataKey.ENDEC);

    public static void handle(SyncWaystonePlayerDataChange packet, PlayerEntity player) {
        WaystonePlayerData.getData(player).setDataUnsafe(packet.key(), packet.data());
    }
}
