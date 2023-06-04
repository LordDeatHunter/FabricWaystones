package wraith.fwaystones;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import wraith.fwaystones.registry.*;
import wraith.fwaystones.util.ConfigModel;
import wraith.fwaystones.util.WaystoneStorage;

public class Waystones {
    public static final String MOD_ID = "fwaystones";
    public static ConfigModel CONFIG;
    public static WaystoneStorage WAYSTONE_STORAGE;
    //public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));
    public static void init() {

        AutoConfig.register(ConfigModel.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ConfigModel.class).getConfig();

        BlockRegistry.BLOCK_REGISTRY.register();
        BlockEntityRegistry.BLOCKENTITY_REGISTRY.register();
        ItemRegistry.ITEM_REGISTRY.register();
        //CompatRegistry.register();
        //CustomScreenHandlerRegistry.register();
        //WaystonesEventManager.register();
        //WaystonePacketHandler.register();


        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
