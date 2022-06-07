package wraith.waystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import wraith.waystones.screen.AbyssScreen;
import wraith.waystones.screen.PocketWormholeScreen;
import wraith.waystones.screen.WaystoneBlockScreen;

@Environment(EnvType.CLIENT)
public final class CustomScreenRegistry {

    public static void registerScreens() {
        HandledScreens.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneBlockScreen::new);
        HandledScreens.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        HandledScreens.register(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, AbyssScreen::new);
    }

}
