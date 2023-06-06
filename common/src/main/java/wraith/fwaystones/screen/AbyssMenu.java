package wraith.fwaystones.screen;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import wraith.fwaystones.item.AbyssWatcherItem;
import wraith.fwaystones.registry.MenuReg;

public class AbyssMenu extends Universalmenu{
	public AbyssMenu(int syncId, Inventory inventory) {
		super(MenuReg.ABYSS_MENU.get(), syncId, inventory.player);
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
