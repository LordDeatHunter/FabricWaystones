package wraith.fwaystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import wraith.fwaystones.screen.AbyssScreen;
import wraith.fwaystones.screen.PocketWormholeScreen;
import wraith.fwaystones.screen.WaystoneBlockScreen;

@Environment(EnvType.CLIENT)
public final class CustomScreenRegistry {

    public static void registerScreens() {
        MenuScreens.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneBlockScreen::new);
        MenuScreens.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        MenuScreens.register(CustomScreenHandlerRegistry.ABYSS_WATCHER_SCREEN, AbyssScreen::new);
    }

}
