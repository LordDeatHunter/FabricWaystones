package wraith.waystones.util;

import net.fabricmc.loader.api.FabricLoader;
import wraith.waystones.interfaces.ServerPlayerEntityTeleporter;

public class TeleporterManager {

    private static ServerPlayerEntityTeleporter teleporter;

    private TeleporterManager() {
    }

    public static void initialize() {
        if (FabricLoader.getInstance().isModLoaded("teleport-utils")) {
            teleporter = new TeleportUtilsTeleporter();
        } else {
            teleporter = new WaystonesTeleporter();
        }
    }

    public static ServerPlayerEntityTeleporter getTeleporter() {
        return teleporter;
    }

}
