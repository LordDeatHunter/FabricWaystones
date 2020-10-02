package wraith.waystones.registries;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import wraith.waystones.screens.WaystoneScreen;

public class CustomScreenRegistry {

    public static void registerScreens() {
        ScreenRegistry.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneScreen::new);
    }

}
