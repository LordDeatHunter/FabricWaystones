package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import wraith.fwaystones.block.WaystoneDataPacket;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.Utils;

public final class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> ABYSS_WATCHER_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("waystone"),
                new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new, WaystoneDataPacket.PACKET_CODEC));

        POCKET_WORMHOLE_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("pocket_wormhole"),
                new ScreenHandlerType<>(PocketWormholeScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        ABYSS_WATCHER_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("abyss"),
                new ScreenHandlerType<>(AbyssScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}