package wraith.waystones.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

public class PocketWormholeScreenHandler extends UniversalWaystoneScreenHandler {

    public PocketWormholeScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.getMainHandStack().getItem() == ItemRegistry.ITEMS.get("pocket_wormhole");
    }

}
