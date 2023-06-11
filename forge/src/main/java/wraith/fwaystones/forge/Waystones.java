package wraith.fwaystones.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(wraith.fwaystones.Waystones.MOD_ID)
public class Waystones {
    public Waystones() {
        // Submit our event bus to let architectury register our content on the right time
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(wraith.fwaystones.Waystones.MOD_ID, eventBus);
        wraith.fwaystones.Waystones.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WaystonesClient.init(eventBus));
    }
}
