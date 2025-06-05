package wraith.fwaystones.integration.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Client side events.
 */
public class WaystoneEvents {

    public static final Event<OnDataUpdate> ON_WAYSTONE_DATA_UPDATE = EventFactory.createArrayBacked(
            OnDataUpdate.class, callbacks -> (uuid, type) -> Arrays.stream(callbacks).forEach(callback -> callback.onChange(uuid, type))
    );

    public static final Event<OnWaystoneDiscovery> ON_WAYSTONE_DISCOVERY = EventFactory.createArrayBacked(
            OnWaystoneDiscovery.class, callbacks -> (player, uuid, position) -> Arrays.stream(callbacks).forEach(callback -> callback.onDiscovery(player, uuid, position))
    );

    public static final Event<OnWaystoneForgotten> ON_WAYSTONE_FORGOTTEN = EventFactory.createArrayBacked(
            OnWaystoneForgotten.class, callbacks -> (player, uuid, position) -> Arrays.stream(callbacks).forEach(callback -> callback.onForgotten(player, uuid, position))
    );

    public static final Event<OnWaystoneForgottenEverything> ON_ALL_WAYSTONES_FORGOTTEN = EventFactory.createArrayBacked(
            OnWaystoneForgottenEverything.class, callbacks -> (player, uuids) -> Arrays.stream(callbacks).forEach(callback -> callback.onForgottenEverything(player, uuids))
    );

    public static final Event<OnPositionUpdate> ON_WAYSTONE_POSITION_UPADTE = EventFactory.createArrayBacked(
            OnPositionUpdate.class, callbacks -> (uuid, position, wasRemoved) -> Arrays.stream(callbacks).forEach(callback -> callback.onChange(uuid, position, wasRemoved))
    );

    public static final Event<OnPlayerDataUpdate> ON_PLAYER_WAYSTONE_DATA_UPDATE = EventFactory.createArrayBacked(
            OnPlayerDataUpdate.class, callbacks -> (player) -> Arrays.stream(callbacks).forEach(callback -> callback.onChange(player))
    );

    @FunctionalInterface
    public interface OnDataUpdate {
        void onChange(UUID uuid, DataChangeType type);
    }

    public interface OnPlayerDataUpdate {
        void onChange(PlayerEntity player);
    }

    @FunctionalInterface
    public interface OnWaystoneDiscovery {
        void onDiscovery(PlayerEntity player, UUID uuid, @Nullable WaystonePosition position);
    }

    @FunctionalInterface
    public interface OnWaystoneForgotten {
        void onForgotten(PlayerEntity player, UUID uuid, @Nullable WaystonePosition position);
    }

    public interface OnWaystoneForgottenEverything {
        void onForgottenEverything(PlayerEntity player, Set<UUID> uuids);
    }

    public interface OnPositionUpdate {
        void onChange(UUID uuid, WaystonePosition position, boolean wasRemoved);
    }
}
