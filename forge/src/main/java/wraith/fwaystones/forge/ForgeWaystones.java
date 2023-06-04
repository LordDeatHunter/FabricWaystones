package wraith.fwaystones.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import wraith.fwaystones.Waystones;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Waystones.MOD_ID)
public class ForgeWaystones {
    public ForgeWaystones() {
        // Submit our event bus to let architectury register our content on the right time
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Waystones.MOD_ID, eventBus);
        Waystones.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ForgeWaystonesClient.init(eventBus));
    }
}
