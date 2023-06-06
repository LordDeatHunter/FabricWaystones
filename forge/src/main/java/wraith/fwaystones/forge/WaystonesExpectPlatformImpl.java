package wraith.fwaystones.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import wraith.fwaystones.WaystonesExpectPlatform;

import java.nio.file.Path;

public class WaystonesExpectPlatformImpl {
    /**
     * This is our actual method to {@link WaystonesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
