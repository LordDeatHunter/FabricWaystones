package wraith.fwaystones.client.registry;

import io.wispforest.endec.StructEndec;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.client.screen.WaystoneScreenOpenDataPacket;
import wraith.fwaystones.client.screen.ExperimentalWaystoneScreenHandler;
import wraith.fwaystones.client.screen.PortableWaystoneScreenHandler;
import wraith.fwaystones.client.screen.WaystoneBlockScreenHandler;

public final class WaystoneScreenHandlers {

    public static final ScreenHandlerType<WaystoneBlockScreenHandler> WAYSTONE_SCREEN = new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new, CodecUtils.toPacketCodec(WaystoneScreenOpenDataPacket.ENDEC));
    public static final ScreenHandlerType<PortableWaystoneScreenHandler> PORTABLE_WAYSTONE_SCREEN = new ScreenHandlerType<>(PortableWaystoneScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static final ScreenHandlerType<ExperimentalWaystoneScreenHandler> EXPERIMENTAL_WAYSTONE_SCREEN = new ExtendedScreenHandlerType<>(
            (syncId, inventory, data) -> new ExperimentalWaystoneScreenHandler(syncId, inventory), CodecUtils.toPacketCodec(StructEndec.unit(() -> new Object[0])));


    public static void init() {
        Registry.register(Registries.SCREEN_HANDLER, FabricWaystones.id("waystone"), WAYSTONE_SCREEN);
        Registry.register(Registries.SCREEN_HANDLER, FabricWaystones.id("portable_waystone"), PORTABLE_WAYSTONE_SCREEN);
        Registry.register(Registries.SCREEN_HANDLER, FabricWaystones.id("experimental_waystone"), EXPERIMENTAL_WAYSTONE_SCREEN);
    }
}
