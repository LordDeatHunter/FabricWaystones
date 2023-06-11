package wraith.fwaystones.screen;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import wraith.fwaystones.item.AbyssWatcherItem;
import wraith.fwaystones.registry.MenuRegister;

public class AbyssScreenHandler extends UniversalWaystoneScreenHandler {

	public AbyssScreenHandler(int syncId, Inventory inventory) {
		super(MenuRegister.ABYSS_MENU.get(), syncId, inventory.player);
		updateWaystones(player);
	}

	@Override
	public void onForget(String waystone) {
	}

	@Override
	public boolean stillValid(Player player) {
		for (var hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).getItem() instanceof AbyssWatcherItem) {
				return true;
			}
		}
		return false;
	}
}
