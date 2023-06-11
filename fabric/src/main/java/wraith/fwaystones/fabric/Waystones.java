package wraith.fwaystones.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import wraith.fwaystones.fabric.integration.pinlib.PinlibPlugin;

public class Waystones implements ModInitializer {
    @Override
    public void onInitialize() {
        wraith.fwaystones.Waystones.init();
        if (FabricLoader.getInstance().isModLoaded("pinlib")) {
            PinlibPlugin.init();
        }
    }
}
