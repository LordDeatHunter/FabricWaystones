package wraith.fwaystones.forge;

import wraith.fwaystones.WaystonesExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WaystonesExpectPlatformImpl {
    /**
     * This is our actual method to {@link WaystonesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
