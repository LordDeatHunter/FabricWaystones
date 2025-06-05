package wraith.fwaystones.client.registry;

import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneScreenOpenDataPacket;
import wraith.fwaystones.client.screen.PortableWaystoneScreenHandler;
import wraith.fwaystones.client.screen.WaystoneBlockScreenHandler;

public final class WaystoneScreenHandlers {

    public static final ScreenHandlerType<WaystoneBlockScreenHandler> WAYSTONE_SCREEN = new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new, CodecUtils.toPacketCodec(WaystoneScreenOpenDataPacket.ENDEC));
    public static final ScreenHandlerType<PortableWaystoneScreenHandler> PORTABLE_WAYSTONE_SCREEN = new ScreenHandlerType<>(PortableWaystoneScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void init() {
        Registry.register(Registries.SCREEN_HANDLER, FabricWaystones.id("waystone"), WAYSTONE_SCREEN);
        Registry.register(Registries.SCREEN_HANDLER, FabricWaystones.id("portable_waystone"), PORTABLE_WAYSTONE_SCREEN);
    }
}