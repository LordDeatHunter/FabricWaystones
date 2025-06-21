package wraith.fwaystones.networking.packets.c2s;

import io.wispforest.endec.StructEndec;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.registry.WaystoneDataComponents;

public record AttemptTeleporterUse() {
    public static final StructEndec<AttemptTeleporterUse> ENDEC = StructEndec.unit(new AttemptTeleporterUse());

    public static void handle(AttemptTeleporterUse packet, PlayerEntity player) {
        var ref = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(player, stack -> stack.contains(WaystoneDataComponents.TELEPORTER));

        if (ref != null) {
            var result = WaystoneComponentEventHooks.useTeleporter(player, ref.get());

            if (result.getResult().isAccepted()) ref.set(result.getValue());

            return;
        }

        ref = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(player, stack -> stack.isIn(FabricWaystones.LOCAL_VOID_ITEM));

        if (ref != null) {
            var result = WaystoneComponentEventHooks.useLocalVoid(player.getWorld(), player, ref.get());

            if (result.getResult().isAccepted()) ref.set(result.getValue());
        }
    }
}
