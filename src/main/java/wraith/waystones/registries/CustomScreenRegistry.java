package wraith.waystones.registries;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.waystones.screens.AbyssScreen;
import wraith.waystones.screens.PocketWormholeScreen;
import wraith.waystones.screens.WaystoneScreen;

public final class CustomScreenRegistry {

    public static void registerScreens() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, AbyssScreen::new);
    }

}
