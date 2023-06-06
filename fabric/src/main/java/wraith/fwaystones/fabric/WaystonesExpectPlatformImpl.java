package wraith.fwaystones.fabric;

import wraith.fwaystones.WaystonesExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class WaystonesExpectPlatformImpl {
    /**
     * This is our actual method to {@link WaystonesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
