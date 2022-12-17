package wraith.fwaystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.fwaystones.screen.AbyssScreen;
import wraith.fwaystones.screen.PocketWormholeScreen;
import wraith.fwaystones.screen.WaystoneBlockScreen;

@Environment(EnvType.CLIENT)
public final class CustomScreenRegistry {

    public static void registerScreens() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneBlockScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        ScreenRegistry.register(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, AbyssScreen::new);
    }

}
