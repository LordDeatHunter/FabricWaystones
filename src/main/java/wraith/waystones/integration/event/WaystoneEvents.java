package wraith.waystones.integration.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.access.WaystoneValue;

/**
 * Client side events.
 */
public class WaystoneEvents
{

    public static final Event<UpdateWaystone> REMOVE_WAYSTONE_EVENT = EventFactory.createArrayBacked(UpdateWaystone.class, callbacks -> event -> {
        for (UpdateWaystone callback : callbacks)
        {
            callback.onUpdate(event);
        }
    });

    public static final Event<UpdateWaystone> DISCOVER_WAYSTONE_EVENT = EventFactory.createArrayBacked(UpdateWaystone.class, callbacks -> event -> {
        for (UpdateWaystone callback : callbacks)
        {
            callback.onUpdate(event);
        }
    });

    public static final Event<RenameWaystone> RENAME_WAYSTONE_EVENT = EventFactory.createArrayBacked(RenameWaystone.class, callbacks -> (event, newName) -> {
        for (RenameWaystone callback : callbacks)
        {
            callback.onRename(event, newName);
        }
    });

    @FunctionalInterface
    public interface UpdateWaystone
    {
        void onUpdate(@Nullable WaystoneValue event);
    }

    @FunctionalInterface
    public interface RenameWaystone
    {
        void onRename(@Nullable WaystoneValue event, String newName);
    }
}
