package wraith.fwaystones.client.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import wraith.fwaystones.client.registry.WaystoneScreenHandlers;

public class ExperimentalWaystoneScreenHandler extends UniversalWaystoneScreenHandler<Object> {
    public ExperimentalWaystoneScreenHandler(int syncId, PlayerInventory inventory) {
        super(WaystoneScreenHandlers.EXPERIMENTAL_WAYSTONE_SCREEN, syncId, inventory.player, null);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // TODO: REWORK TO CHECK IF VALID LIKE OLD SCREEN

        return true;
    }
}
