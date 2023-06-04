package wraith.fwaystones.fabric;

import net.fabricmc.api.ClientModInitializer;
import wraith.fwaystones.Waystones;
import net.fabricmc.api.ModInitializer;

public class FabricWaystones implements ModInitializer {
    @Override
    public void onInitialize() {
        Waystones.init();
    }
}
