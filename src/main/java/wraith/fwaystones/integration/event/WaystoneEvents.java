package wraith.fwaystones.integration.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Client side events.
 */
public class WaystoneEvents {

    public static final Event<UpdateWaystone> DISCOVER_WAYSTONE_EVENT = EventFactory.createArrayBacked(
        UpdateWaystone.class, callbacks -> hash -> Arrays.stream(callbacks).forEach(callback -> callback.onUpdate(hash))
    );
    public static final Event<ForgetAllWaystones> FORGET_ALL_WAYSTONES_EVENT = EventFactory.createArrayBacked(
        ForgetAllWaystones.class, callbacks -> player -> Arrays.stream(callbacks).forEach(callback -> callback.onForgetAll(player))
    );
    public static final Event<RemoveWaystone> REMOVE_WAYSTONE_EVENT = EventFactory.createArrayBacked(
        RemoveWaystone.class, callbacks -> hash -> Arrays.stream(callbacks).forEach(callback -> callback.onRemove(hash))
    );
    public static final Event<UpdateWaystone> RENAME_WAYSTONE_EVENT = EventFactory.createArrayBacked(
        UpdateWaystone.class, callbacks -> hash -> Arrays.stream(callbacks).forEach(callback -> callback.onUpdate(hash))
    );

    @FunctionalInterface
    public interface UpdateWaystone {

        void onUpdate(@Nullable String hash);
    }

    @FunctionalInterface
    public interface RemoveWaystone {

        void onRemove(@Nullable String hash);
    }

    @FunctionalInterface
    public interface ForgetAllWaystones {

        void onForgetAll(PlayerEntity player);
    }
}
