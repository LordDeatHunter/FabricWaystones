package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.WaystoneDataStorage;

public record SyncWaystoneStorage(WaystoneDataStorage storage) {
    public static final StructEndec<SyncWaystoneStorage> ENDEC = StructEndecBuilder.of(
            WaystoneDataStorage.ENDEC.fieldOf("data", SyncWaystoneStorage::storage),
            SyncWaystoneStorage::new
    );

    public static void handle(SyncWaystoneStorage packet, PlayerEntity player) {
        WaystoneDataStorage.setClientStorage(packet.storage, player);
    }
}
