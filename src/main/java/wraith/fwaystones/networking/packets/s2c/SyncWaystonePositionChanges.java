package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public record SyncWaystonePositionChanges(List<SyncWaystonePositionChange> changes) {
    public static final StructEndec<SyncWaystonePositionChanges> ENDEC = StructEndecBuilder.of(
            SyncWaystonePositionChange.ENDEC.listOf().fieldOf("changes", SyncWaystonePositionChanges::changes),
            SyncWaystonePositionChanges::new
    );

    public static void handle(SyncWaystonePositionChanges packet, PlayerEntity player) {
        for (var change : packet.changes()) {
            SyncWaystonePositionChange.handle(change, player);
        }
    }
}
