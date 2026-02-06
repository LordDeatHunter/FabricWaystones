package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;

public record WaystoneGUISlotClick(int syncId, int clickedSlot) {
    public static final StructEndec<WaystoneGUISlotClick> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("sync_id", WaystoneGUISlotClick::syncId),
            Endec.VAR_INT.fieldOf("clicked_slot", WaystoneGUISlotClick::clickedSlot),
            WaystoneGUISlotClick::new
    );

    public static void handle(WaystoneGUISlotClick packet, PlayerEntity player) {
        if (player.currentScreenHandler.syncId == packet.syncId()) {
            player.currentScreenHandler.onButtonClick(player, packet.clickedSlot());
        }
    }
}
