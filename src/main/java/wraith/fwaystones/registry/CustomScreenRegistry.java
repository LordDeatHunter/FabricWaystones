package wraith.fwaystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import wraith.fwaystones.screen.AbyssScreen;
import wraith.fwaystones.screen.PocketWormholeScreen;
import wraith.fwaystones.screen.WaystoneBlockScreen;

@Environment(EnvType.CLIENT)
public final class CustomScreenRegistry {

    public static void registerScreens() {
        HandledScreens.register(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, WaystoneBlockScreen::new);
        HandledScreens.register(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN, PocketWormholeScreen::new);
        HandledScreens.register(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, AbyssScreen::new);
    }

}
