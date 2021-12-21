package wraith.waystones.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import wraith.waystones.util.Utils;
import wraith.waystones.screen.AbyssScreenHandler;
import wraith.waystones.screen.PocketWormholeScreenHandler;
import wraith.waystones.screen.UniversalWaystoneScreenHandler;
import wraith.waystones.screen.WaystoneScreenHandler;

public final class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> ABYSS_SCREEN_HANDLER;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = ScreenHandlerRegistry.registerExtended(Utils.ID("waystone"), WaystoneScreenHandler::new);
        POCKET_WORMHOLE_SCREEN = ScreenHandlerRegistry.registerSimple(Utils.ID("pocket_wormhole"), PocketWormholeScreenHandler::new);
        ABYSS_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Utils.ID("abyss"), AbyssScreenHandler::new);
    }

}
