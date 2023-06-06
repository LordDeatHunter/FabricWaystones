package wraith.fwaystones.forge;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;
import wraith.fwaystones.WaystonesExpectPlatform;

import java.nio.file.Path;

public class WaystonesExpectPlatformImpl {
    public static void openExtendedMenu(ServerPlayer player, ExtendedMenuProvider provider) {
        NetworkHooks.openScreen(player, provider, provider::saveExtraData);
    }
    /**
     * This is our actual method to {@link WaystonesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
