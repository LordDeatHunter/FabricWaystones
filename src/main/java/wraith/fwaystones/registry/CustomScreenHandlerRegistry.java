package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;

public final class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> ABYSS_SCREEN_HANDLER;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = ScreenHandlerRegistry.registerExtended(Utils.ID("waystone"), WaystoneBlockScreenHandler::new);
        POCKET_WORMHOLE_SCREEN = ScreenHandlerRegistry.registerSimple(Utils.ID("pocket_wormhole"), PocketWormholeScreenHandler::new);
        ABYSS_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Utils.ID("abyss"), AbyssScreenHandler::new);
    }

}
