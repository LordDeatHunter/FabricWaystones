package wraith.waystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.waystones.screen.AbyssScreen;
import wraith.waystones.screen.PocketWormholeScreen;
import wraith.waystones.screen.WaystoneScreen;

@Environment(EnvType.CLIENT)
public final class CustomScreenRegistry {

    public static void registerScreens() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, AbyssScreen::new);
    }

}
