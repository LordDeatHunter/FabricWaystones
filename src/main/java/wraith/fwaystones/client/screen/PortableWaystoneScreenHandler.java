package wraith.fwaystones.client.screen;

import net.minecraft.entity.player.PlayerInventory;
import wraith.fwaystones.client.registry.WaystoneScreenHandlers;
import wraith.fwaystones.registry.WaystoneDataComponents;

public class PortableWaystoneScreenHandler extends UniversalWaystoneScreenHandler<Void> {
    public PortableWaystoneScreenHandler(int syncId, PlayerInventory inventory) {
        super(WaystoneScreenHandlers.PORTABLE_WAYSTONE_SCREEN, syncId, inventory.player, null);
    }

    public boolean isAbyssal() {
        for (var handItem : player.getHandItems()) {
            var data = handItem.get(WaystoneDataComponents.TELEPORTER);

            if (data != null && data.oneTimeUse()) return true;
        }

        return false;
    }
}
