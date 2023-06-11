package wraith.fwaystones.screen;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import wraith.fwaystones.item.PocketWormholeItem;
import wraith.fwaystones.registry.MenuRegister;

public class PocketWormholeScreenHandler extends UniversalWaystoneScreenHandler {

	public PocketWormholeScreenHandler(int syncId, Inventory inventory) {
		super(MenuRegister.POCKET_WORMHOLE_MENU.get(), syncId, inventory.player);
		updateWaystones(player);
	}

	@Override
	public void onForget(String waystone) {}

	@Override
	public boolean stillValid(Player player) {
		for (var hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).getItem() instanceof PocketWormholeItem) {
				return true;
			}
		}
		return false;
	}
}
