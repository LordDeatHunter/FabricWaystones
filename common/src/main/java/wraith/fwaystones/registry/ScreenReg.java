package wraith.fwaystones.registry;

import dev.architectury.registry.menu.MenuRegistry;
import wraith.fwaystones.screen.AbyssScreen;
import wraith.fwaystones.screen.PocketWormholeScreen;
import wraith.fwaystones.screen.WaystoneScreen;

public class ScreenReg {
	public static void register() {
		MenuRegistry.registerScreenFactory(MenuReg.ABYSS_MENU.get(), AbyssScreen::new);
		MenuRegistry.registerScreenFactory(MenuReg.WAYSTONE_MENU.get(), WaystoneScreen::new);
		MenuRegistry.registerScreenFactory(MenuReg.POCKET_WORMHOLE_MENU.get(), PocketWormholeScreen::new);
	}
}
