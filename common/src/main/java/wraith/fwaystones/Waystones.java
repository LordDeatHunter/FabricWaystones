package wraith.fwaystones;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.fwaystones.registry.BlockEntityRegister;
import wraith.fwaystones.registry.BlockRegister;
import wraith.fwaystones.registry.ItemRegister;
import wraith.fwaystones.registry.MenuRegister;
import wraith.fwaystones.util.EventManager;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.WaystoneStorage;
import wraith.fwaystones.util.Config;

public class Waystones {
    public static final String MOD_ID = "fwaystones";
    public static Config CONFIG;
    public static WaystoneStorage STORAGE;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID.toUpperCase());
    public static void init() {
        Config.register();

        BlockRegister.BLOCK_REGISTRY.register();
        BlockEntityRegister.BLOCKENTITY_REGISTRY.register();
        ItemRegister.ITEM_REGISTRY.register();
        //CompatReg.register();
        MenuRegister.MENU_REGISTRY.register();
        EventManager.registerServer();
        PacketHandler.registerC2SListeners();
    }
}
