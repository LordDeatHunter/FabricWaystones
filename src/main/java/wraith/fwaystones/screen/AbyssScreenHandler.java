package wraith.fwaystones.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import wraith.fwaystones.item.AbyssWatcherItem;
import wraith.fwaystones.registry.CustomScreenHandlerRegistry;

public class AbyssScreenHandler extends UniversalWaystoneScreenHandler {

    public AbyssScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, syncId, inventory.player);
        updateWaystones(player);
    }

    @Override
    public void onForget(String waystone) {
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        for (var hand : Hand.values()) {
            if (player.getStackInHand(hand).getItem() instanceof AbyssWatcherItem) {
                return true;
            }
        }
        return false;
    }

}
