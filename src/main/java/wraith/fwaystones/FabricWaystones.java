package wraith.fwaystones;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.fwaystones.registry.*;
import wraith.fwaystones.util.FWConfig;
import wraith.fwaystones.util.WaystonePacketHandler;
import wraith.fwaystones.util.WaystoneStorage;
import wraith.fwaystones.util.WaystonesEventManager;

public class FabricWaystones implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Fabric-Waystones");
    public static final String MOD_ID = "fwaystones";
    public static WaystoneStorage WAYSTONE_STORAGE;
    public static final FWConfig CONFIG = FWConfig.createAndLoad();

    @Override
    public void onInitialize() {
        LOGGER.info("Is initializing.");
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.init();
        CompatRegistry.init();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        WaystonesEventManager.registerEvents();
        WaystonePacketHandler.registerPacketHandlers();

        LOGGER.info("Has successfully been initialized.");
    }

}
