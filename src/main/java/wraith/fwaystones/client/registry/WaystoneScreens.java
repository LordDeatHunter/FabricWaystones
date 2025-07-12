package wraith.fwaystones.client.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import wraith.fwaystones.client.screen.ExperimentalWaystoneScreen;
import wraith.fwaystones.client.screen.PortableWaystoneScreen;
import wraith.fwaystones.client.screen.WaystoneBlockScreen;

@Environment(EnvType.CLIENT)
public final class WaystoneScreens {
    public static void register() {
        HandledScreens.register(WaystoneScreenHandlers.WAYSTONE_SCREEN, WaystoneBlockScreen::new);
        HandledScreens.register(WaystoneScreenHandlers.EXPERIMENTAL_WAYSTONE_SCREEN, ExperimentalWaystoneScreen::new);
        HandledScreens.register(WaystoneScreenHandlers.PORTABLE_WAYSTONE_SCREEN, PortableWaystoneScreen::new);
    }
}
