package wraith.fwaystones;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.fwaystones.registry.*;
import wraith.fwaystones.util.ConfigModel;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.WaystoneStorage;
import wraith.fwaystones.util.EventManager;

public class Waystones {
    public static final String MOD_ID = "fwaystones";
    public static ConfigModel CONFIG;
    public static WaystoneStorage WAYSTONE_STORAGE;
    public static final Logger LOGGER = LogManager.getLogger("F-Waystones");
    //public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));
    public static void init() {
        ConfigModel.register();

        BlockReg.BLOCK_REGISTRY.register();
        BlockEntityReg.BLOCKENTITY_REGISTRY.register();
        ItemReg.ITEM_REGISTRY.register();
        //CompatReg.register();
        //MenuReg.MENU_REGISTRY.register();//CustomScreenHandlerRegistry.register();
        EventManager.registerServer();
        PacketHandler.registerC2SListeners();

        System.out.println(WaystonesExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
