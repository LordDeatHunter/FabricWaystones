package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.Utils;

public final class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> ABYSS_SCREEN_HANDLER;
    public static ExtendedScreenHandlerType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = Registry.register(Registry.SCREEN_HANDLER, Utils.ID("waystone"), new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new));
        POCKET_WORMHOLE_SCREEN = Registry.register(Registry.SCREEN_HANDLER, Utils.ID("pocket_wormhole"), new ScreenHandlerType<>(PocketWormholeScreenHandler::new));
        ABYSS_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, Utils.ID("abyss"), new ScreenHandlerType<>(AbyssScreenHandler::new));
    }

}
