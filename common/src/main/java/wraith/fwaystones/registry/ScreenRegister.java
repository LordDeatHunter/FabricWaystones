package wraith.fwaystones.registry;

import dev.architectury.registry.menu.MenuRegistry;
import wraith.fwaystones.screen.AbyssScreen;
import wraith.fwaystones.screen.PocketWormholeScreen;
import wraith.fwaystones.screen.WaystoneBlockScreen;

public class ScreenRegister {
	public static void register() {
		MenuRegistry.registerScreenFactory(MenuRegister.ABYSS_MENU.get(), AbyssScreen::new);
		MenuRegistry.registerScreenFactory(MenuRegister.WAYSTONE_MENU.get(), WaystoneBlockScreen::new);
		MenuRegistry.registerScreenFactory(MenuRegister.POCKET_WORMHOLE_MENU.get(), PocketWormholeScreen::new);
	}
}
