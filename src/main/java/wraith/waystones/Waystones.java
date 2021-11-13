package wraith.waystones;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.waystones.compat.RepurposedStructuresCompat;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.registries.BlockRegistry;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;
import wraith.waystones.util.Config;
import wraith.waystones.util.WaystonePacketHandler;
import wraith.waystones.util.WaystoneStorage;
import wraith.waystones.util.WaystonesEventManager;

public class Waystones implements ModInitializer, DedicatedServerModInitializer {

    public static final String MOD_ID = "waystones";
    public static WaystoneStorage WAYSTONE_STORAGE;
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("[Fabric-Waystones] is initializing.");
        Config.getInstance().loadConfig();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.init();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        WaystonesEventManager.registerEvents();
        WaystonePacketHandler.registerPacketHandlers();
        LOGGER.info("[Fabric-Waystones] has successfully been initialized.");
        LOGGER.info("[Fabric-Waystones] If you have any issues or questions, feel free to join our Discord: https://discord.gg/vMjzgS4.");
    }

    @Override
    public void onInitializeServer() {
        if (FabricLoader.getInstance().isModLoaded("repurposed_structures") && Config.getInstance().generateInVillages()) {
            RepurposedStructuresCompat.init();
        }
    }

}
