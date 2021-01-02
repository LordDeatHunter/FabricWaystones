package wraith.waystones.registries;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.waystones.Utils;
import wraith.waystones.screens.PocketWormholeScreenHandler;
import wraith.waystones.screens.WaystoneScreenHandler;

public class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends ScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends ScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = ScreenHandlerRegistry.registerExtended(Utils.ID("waystone"), WaystoneScreenHandler::new);
        POCKET_WORMHOLE_SCREEN = ScreenHandlerRegistry.registerSimple(Utils.ID("pocket_wormhole"), PocketWormholeScreenHandler::new);
    }

}
