package wraith.waystones.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

public class PocketWormholeScreenHandler extends UniversalWaystoneScreenHandler {

    public PocketWormholeScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, syncId, inventory.player);
    }

    @Override
    public void onForget(String waystone) {

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).getItem() == ItemRegistry.ITEMS.get("pocket_wormhole") || player.getStackInHand(Hand.OFF_HAND).getItem() == ItemRegistry.ITEMS.get("pocket_wormhole");
    }

}
