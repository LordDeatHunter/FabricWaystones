package wraith.fwaystones;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.fwaystones.registry.BlockEntityRegistry;
import wraith.fwaystones.registry.BlockRegistry;
import wraith.fwaystones.registry.ItemRegistry;
import wraith.fwaystones.registry.TabRegistry;
import wraith.fwaystones.util.EventManager;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.Storage;
import wraith.fwaystones.util.TODO_ConfigModel;

public class Waystones {
    public static final String MOD_ID = "fwaystones";
    public static TODO_ConfigModel CONFIG;
    public static Storage WAYSTONE_STORAGE;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID.toUpperCase());
    //public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));
    public static void init() {
        TODO_ConfigModel.register();

        BlockRegistry.BLOCK_REGISTRY.register();
        BlockEntityRegistry.BLOCKENTITY_REGISTRY.register();
        ItemRegistry.ITEM_REGISTRY.register();
        TabRegistry.register();
        //CompatReg.register();
        //MenuReg.MENU_REGISTRY.register();
        EventManager.registerServer();
        PacketHandler.registerC2SListeners();

        System.out.println(WaystonesExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
