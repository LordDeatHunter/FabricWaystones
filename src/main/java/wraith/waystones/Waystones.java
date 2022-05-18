package wraith.waystones;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.waystones.registry.*;
import wraith.waystones.util.Config;
import wraith.waystones.util.WaystonePacketHandler;
import wraith.waystones.util.WaystoneStorage;
import wraith.waystones.util.WaystonesEventManager;

public class Waystones implements ModInitializer {

    public static final String MOD_ID = "waystones";
    public static WaystoneStorage WAYSTONE_STORAGE;
    public static final Logger LOGGER = LogManager.getLogger("Fabric-Waystones");

    @Override
    public void onInitialize() {
        LOGGER.info("Is initializing.");
        Config.getInstance().loadConfig();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.init();
        CompatRegistry.init();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        WaystonesEventManager.registerEvents();
        WaystonePacketHandler.registerPacketHandlers();

        LOGGER.info("Has successfully been initialized.");
        LOGGER.info("If you have any issues or questions, feel free to join our Discord: https://discord.gg/vMjzgS4.");
    }

}
