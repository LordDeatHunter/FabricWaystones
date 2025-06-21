package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public record SyncWaystoneDataChanges(List<SyncWaystoneDataChange> changes) {
    public static final StructEndec<SyncWaystoneDataChanges> ENDEC = StructEndecBuilder.of(
            SyncWaystoneDataChange.ENDEC.listOf().fieldOf("changes", SyncWaystoneDataChanges::changes),
            SyncWaystoneDataChanges::new
    );

    public static void handle(SyncWaystoneDataChanges changes, PlayerEntity player) {
        for (var change : changes.changes()) {
            SyncWaystoneDataChange.handle(change, player);
        }
    }
}
