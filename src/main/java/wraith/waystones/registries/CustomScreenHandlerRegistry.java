package wraith.waystones.registries;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import wraith.waystones.screens.*;
import wraith.waystones.Waystones;

import java.util.HashMap;

public class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends ScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = ScreenHandlerRegistry.registerExtended(new Identifier(Waystones.MOD_ID, "waystone"), WaystoneScreenHandler::new);
    }

}
