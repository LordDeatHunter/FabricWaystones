package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import wraith.fwaystones.block.WaystoneDataPacket;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.Utils;

public final class CustomScreenHandlerRegistry {

    public static MenuType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static MenuType<? extends UniversalWaystoneScreenHandler> ABYSS_WATCHER_SCREEN;
    public static MenuType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = Registry.register(BuiltInRegistries.MENU,
                Utils.ID("waystone"),
                new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new, WaystoneDataPacket.PACKET_CODEC));

        POCKET_WORMHOLE_SCREEN = Registry.register(BuiltInRegistries.MENU,
                Utils.ID("pocket_wormhole"),
                new MenuType<>(PocketWormholeScreenHandler::new, FeatureFlags.VANILLA_SET));

        ABYSS_WATCHER_SCREEN = Registry.register(BuiltInRegistries.MENU,
                Utils.ID("abyss"),
                new MenuType<>(AbyssScreenHandler::new, FeatureFlags.VANILLA_SET));
    }
}