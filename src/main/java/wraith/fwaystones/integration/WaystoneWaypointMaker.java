package wraith.fwaystones.integration;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WaystoneWaypointMaker<T> {

    public boolean shouldResetAllWaypoints = false;

    public final Map<UUID, T> uuidToPoint = new ConcurrentHashMap<>();
    public final Map<T, UUID> pointToUUID = new ConcurrentHashMap<>();

    public final Map<T, WaystonePosition> pointToPosition = new ConcurrentHashMap<>();

    private final Set<UUID> queuedWaypoints = ConcurrentHashMap.newKeySet();

    private boolean alreadySetup = false;

    public WaystoneWaypointMaker() {
        this.setupEvents();
    }

    public void setupEvents() {
        if (alreadySetup) return;

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            var storage = getStorage();
            if (storage == null) return;

            var pos = storage.getPosition(uuid);
            if (pos == null) return;

            deleteWaypoint(uuid);
            if (type != DataChangeType.REMOVAL) addWaypoint(uuid);
        });
        WaystoneEvents.ON_WAYSTONE_DISCOVERY.register((player, uuid, pos) -> {
            if (pos == null) return;

            addWaypoint(uuid);
        });
        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.register((player, uuid, pos) -> {
            if (pos == null) return;

            deleteWaypoint(uuid);
        });
        WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.register((p, uuids) -> {
            var storage = getStorage();
            if (storage == null) return;

            deleteAllWaypoints();
        });
        WaystoneEvents.ON_PLAYER_WAYSTONE_DATA_UPDATE.register(player -> {
            this.shouldResetAllWaypoints = true;
        });
        WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.register((uuid, position, wasRemoved) -> {
            if (wasRemoved) {
                deleteWaypoint(uuid);
            } else {
                addWaypoint(uuid);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.shouldResetAllWaypoints) {
                resetWaystones();
                this.shouldResetAllWaypoints = false;
            } else if (validToCreateWaypoints() && !this.queuedWaypoints.isEmpty()) {
                this.addWaypoints(this.queuedWaypoints);

                this.queuedWaypoints.clear();
            }
        });

        alreadySetup = false;
    }

    //--

    protected void deleteWaypoint(UUID uuid) {
        if (!isEnabled()) return;

        var point = uuidToPoint.remove(uuid);

        if (point != null) {
            pointToUUID.remove(point);
            deleteWaypointFromPosition(point);
        }
    }

    protected void deleteAllWaypoints() {
        if (!isEnabled()) return;

        for (var entry : uuidToPoint.entrySet()) {
            deleteWaypointFromPosition(entry.getValue());
        }

        uuidToPoint.clear();
        pointToUUID.clear();
    }

    @Nullable
    protected WaystonePosition deleteWaypointFromPosition(T point) {
        if (!isEnabled()) return null;

        return pointToPosition.remove(point);
    }

    @Nullable
    public UUID getWaystoneUUID(T waypoint) {
        if (!isEnabled()) return null;

        return pointToUUID.get(waypoint);
    }

    @Nullable
    public NetworkedWaystoneData getWaystoneData(T waypoint) {
        var uuid = getWaystoneUUID(waypoint);

        if (uuid != null) {
            var storage = WaystoneDataStorage.getStorage(MinecraftClient.getInstance());

            if (storage != null) return storage.getData(uuid);
        }

        return null;
    }

    protected void addWaypoint(UUID uuid) {
        if (!isEnabled() || !displayWaypoints()) return;

        var storage = getStorage();
        if (storage == null || !validToCreateWaypoints()) {
            queuedWaypoints.add(uuid);
            return;
        }

        var pos = storage.getPosition(uuid);
        if (pos == null) return;

        var data = storage.getData(pos);
        if (data == null) return;

        var waypoint = createWaypoint(pos, data);

        deleteWaypoint(data.uuid());

        uuidToPoint.put(data.uuid(), waypoint);
        pointToUUID.put(waypoint, data.uuid());

        pointToPosition.put(waypoint, pos);
    }

    public void resetWaystones() {
        resetWaystones(getClientValidDiscovered());
    }

    public void resetWaystones(Collection<UUID> waystones) {
        if(!isEnabled() || !validToCreateWaypoints()) return;

        deleteAllWaypoints();

        addWaypoints(waystones);
    }

    public void addWaypoints(Collection<UUID> waystones) {
        if(!isEnabled() || !validToCreateWaypoints()) return;

        try {
            if (!displayWaypoints()) return;

            if (!validToCreateWaypoints()) {
                this.queuedWaypoints.addAll(waystones);

                return;
            }

            for (var uuid : waystones) {
                try {
                    addWaypoint(uuid);
                } catch (Exception e) {
                    FabricWaystones.LOGGER.error("An exception has occured when attempting to sync a waystone position: [Compat Id: {}, UUID: {}]", getId(), uuid, e);
                }
            }
        } catch (Exception e) {
            FabricWaystones.LOGGER.error("An exception has occured when attempting to add a waystones waypoint! [Compat Id: {}]", getId(), e);
        }
    }

    //--

    public abstract T createWaypoint(WaystonePosition pos, NetworkedWaystoneData data);

    public abstract boolean validToCreateWaypoints();

    public abstract boolean isEnabled();

    public abstract boolean displayWaypoints();

    public abstract Identifier getId();

    //--

    @Nullable
    public static WaystoneDataStorage getStorage() {
        return WaystoneDataStorage.getStorage(MinecraftClient.getInstance());
    }

    @Nullable
    public static WaystonePlayerData getPlayerData() {
        var player = MinecraftClient.getInstance().player;

        if (player == null) return null;

        return WaystonePlayerData.getData(player);
    }

    public static Collection<UUID> getClientValidDiscovered() {
        var data = getPlayerData();
        var storage = getStorage();

        if (data != null && storage != null) {
            var discoveredWaystones = data.discoveredWaystones();

            return discoveredWaystones.stream()
                    .filter(uuid -> {
                        var pos = storage.getPosition(uuid);

                        return pos != null;
                    })
                    .toList();
        }

        return Collections.emptyList();
    }
}
