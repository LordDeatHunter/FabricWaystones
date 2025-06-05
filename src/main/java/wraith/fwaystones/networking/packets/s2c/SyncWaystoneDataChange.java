package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.UUID;

public record SyncWaystoneDataChange(UUID uuid, @Nullable WaystoneData data, DataChangeType change) {
    public static final StructEndec<SyncWaystoneDataChange> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", SyncWaystoneDataChange::uuid),
            WaystoneData.ENDEC.nullableOf().fieldOf("data", SyncWaystoneDataChange::data),
            Endec.forEnum(DataChangeType.class).fieldOf("change", SyncWaystoneDataChange::change),
            SyncWaystoneDataChange::new
    );

    public static void handle(SyncWaystoneDataChange packet, PlayerEntity player) {
        WaystoneDataStorage.getStorage(player).onSyncData(packet.uuid(), packet.data(), packet.change());
    }
}
