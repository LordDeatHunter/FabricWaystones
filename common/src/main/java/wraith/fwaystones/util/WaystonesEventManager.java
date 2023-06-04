package wraith.fwaystones.util;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import wraith.fwaystones.Waystones;

public class WaystonesEventManager {
    public static void register() {
        LifecycleEvent.SERVER_STARTED.register((server)->{
            if (Waystones.WAYSTONE_STORAGE == null) {
                Waystones.WAYSTONE_STORAGE = new WaystoneStorage(server);
            }
        });
        LifecycleEvent.SERVER_STOPPED.register((server)->{
            if (Waystones.WAYSTONE_STORAGE == null) {
                if (server.isDedicatedServer())
                    System.out.println("The Waystone storage is null. This is likely caused by a crash.");
                    //FabricWaystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            Waystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            Waystones.WAYSTONE_STORAGE = null;
        });

    }
}
