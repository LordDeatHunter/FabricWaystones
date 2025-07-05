package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.UUID;

public record SyncWaystonePositionChange(UUID uuid, @Nullable WaystonePosition position) {
    public static final StructEndec<SyncWaystonePositionChange> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", SyncWaystonePositionChange::uuid),
            WaystonePosition.ENDEC.nullableOf().fieldOf("position", SyncWaystonePositionChange::position),
            SyncWaystonePositionChange::new
    );

    public static void handle(SyncWaystonePositionChange packet, PlayerEntity player) {
        WaystoneDataStorage.getStorage(player).onSyncPosition(packet.uuid(), packet.position());
    }
}
