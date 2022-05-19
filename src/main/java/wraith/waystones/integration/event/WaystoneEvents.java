package wraith.waystones.integration.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

/**
 * Client side events.
 */
public class WaystoneEvents
{

    public static final Event<RemoveWaystone> REMOVE_WAYSTONE_EVENT = EventFactory.createArrayBacked(RemoveWaystone.class, callbacks -> hash -> {
        for (RemoveWaystone callback : callbacks)
        {
            callback.onRemove(hash);
        }
    });

    public static final Event<UpdateWaystone> DISCOVER_WAYSTONE_EVENT = EventFactory.createArrayBacked(UpdateWaystone.class, callbacks -> event -> {
        for (UpdateWaystone callback : callbacks)
        {
            callback.onUpdate(event);
        }
    });

    public static final Event<UpdateWaystone> RENAME_WAYSTONE_EVENT = EventFactory.createArrayBacked(UpdateWaystone.class, callbacks -> event -> {
        for (UpdateWaystone callback : callbacks)
        {
            callback.onUpdate(event);
        }
    });

    @FunctionalInterface
    public interface UpdateWaystone
    {
        void onUpdate(@Nullable String hash);
    }

    @FunctionalInterface
    public interface RemoveWaystone
    {
        void onRemove(@Nullable String hash);
    }
}
