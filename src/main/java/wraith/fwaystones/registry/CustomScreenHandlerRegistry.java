package wraith.fwaystones.registry;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.fwaystones.block.WaystoneDataPacket;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;
import wraith.fwaystones.util.Utils;

public final class CustomScreenHandlerRegistry {

    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> POCKET_WORMHOLE_SCREEN;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> ABYSS_SCREEN_HANDLER;
    public static ScreenHandlerType<? extends UniversalWaystoneScreenHandler> WAYSTONE_SCREEN;


    public static void registerScreenHandlers() {
        WAYSTONE_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("waystoneHash"),
                new ExtendedScreenHandlerType<>(WaystoneBlockScreenHandler::new, WaystoneDataPacket.PACKET_CODEC));

        POCKET_WORMHOLE_SCREEN = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("pocket_wormhole"),
                new ScreenHandlerType<>(PocketWormholeScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

        ABYSS_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER,
                Utils.ID("abyss"),
                new ScreenHandlerType<>(AbyssScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}