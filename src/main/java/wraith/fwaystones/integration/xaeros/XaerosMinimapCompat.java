package wraith.fwaystones.integration.xaeros;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.api.WaystoneEvents;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointVisibilityType;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XaerosMinimapCompat {

    public static final XaerosMinimapCompat INSTANCE = new XaerosMinimapCompat();

    XaerosMinimapCompat(){}

    public boolean shouldSync = false;

    private WaypointColor color = null;
    private boolean useSeperateSet = false;
    private WaypointVisibilityType visibilityType = WaypointVisibilityType.LOCAL;

    public void color(@Nullable WaypointColor color) {
        this.color = color;

        shouldSync = true;
    }

    public void useSeperateSet(boolean waypointSet) {
        this.useSeperateSet = waypointSet;

        shouldSync = true;
    }

    public void visibilityType(WaypointVisibilityType type) {
        this.visibilityType = type;

        shouldSync = true;
    }

    public void setupEvents() {
        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            var storage = getStorage();
            if (storage == null) return;

            var pos = storage.getPosition(uuid);
            if (pos == null) return;

            removeWaystone(pos);
            addWaystone(pos, getOrCreateSet(pos));
        });
        WaystoneEvents.ON_WAYSTONE_DISCOVERY.register((player, uuid, pos) -> {
            if (pos == null) return;

            addWaystone(pos, getOrCreateSet(pos));
        });
        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.register((player, uuid, pos) -> {
            if (pos == null) return;

            removeWaystone(pos);
        });
        WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.register((p, uuids) -> {
            var storage = getStorage();
            if (storage == null) return;

            for (var uuid : uuids) {
                var pos = storage.getPosition(uuid);

                if (pos == null) continue;

                removeWaystone(pos);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.shouldSync) {
                resetWaystones(getClientValidDiscovered());
                this.shouldSync = false;
            }
        });
        WaystoneEvents.ON_PLAYER_WAYSTONE_DATA_UPDATE.register(player -> {
            this.shouldSync = true;
        });
    }

    private boolean resetWaystones(Collection<WaystonePosition> waystones) {
        var minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) return false;

        var worldManager = minimapSession.getWorldManager();
        if (worldManager == null) return false;

        var currentWorld = worldManager.getCurrentWorld();
        if (currentWorld == null) return false;

        try {
            getAllWaypoints().forEach(waypoints -> {
                waypoints.removeIf(XaerosMinimapCompat::isWaypointFromWaystone);
            });

            for (var pos : waystones) {
                try {
                    addWaystone(pos, getOrCreateSet(pos));
                } catch (final Exception e) {
                    FabricWaystones.LOGGER.error("An exception has occured when attempting to sync a waystone position: [Pos: {}]", pos, e);
                }
            }
        } catch (Exception e) {
            FabricWaystones.LOGGER.error("An exception has occured when attempting to sync waystones!", e);
        }

        return true;
    }

    private void removeWaystone(WaystonePosition position) {
        getAllWaypoints().forEach(waypoints -> {
            waypoints.removeIf(waypoint -> {
                return isWaypointFromWaystone(waypoint)
                        && position.blockPos().equals(new BlockPos(waypoint.getX(), waypoint.getY(), waypoint.getZ()));
            });
        });
    }

    private static boolean isWaypointFromWaystone(Waypoint waypoint) {
        return waypoint.isTemporary() && waypoint.getName().endsWith(" [Wraith Waystone]");
    }

    private void addWaystone(final WaystonePosition pos, final WaypointSet waypointsList) {
        var blockPos = pos.blockPos();

        var storage = getStorage();
        if (storage == null) return;

        var data = storage.getData(pos);
        if (data == null) return;

        var waypoint = new Waypoint(
                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                "[Wraith Waystone: " + data.uuid().toString() + "]",
                "",
                color == null ? WaypointColor.getRandom() : color,
                WaypointPurpose.NORMAL,
                true
        );

        waypoint.setVisibility(visibilityType);

        waypointsList.add(waypoint);
    }

    private WaypointSet getOrCreateSet(final WaystonePosition position) {
        var waypointWorld = getWorldOrThrow(position);

        return getOrCreateSet(waypointWorld, this.useSeperateSet ? "Wraith Waystones" : "gui.xaero_default");
    }

    //--

    public static WaypointSet getOrCreateSet(MinimapWorld world, String name) {
        var waypointSet = world.getWaypointSet(name);

        if (waypointSet == null) {
            world.addWaypointSet(name);

            waypointSet = world.getWaypointSet(name);
        }

        return waypointSet;
    }

    public static MinimapWorld getWorldOrThrow(WaystonePosition position) {
        var world = getWorld(position.worldName());

        if (world == null) throw new NullPointerException("Unable to locate the targeted WaystonePosition world! [Pos: " + position + "]");

        return world;
    }

    public static MinimapWorld getWorld(String worldName) {
        var dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldName));

        var minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) return null;

        var manager = minimapSession.getWorldManager();

        var currentWorld = manager.getCurrentWorld();
        if (currentWorld != null && currentWorld.getDimId().equals(dimensionKey)) return currentWorld;

        var targetedWorld = toStream(manager.getCurrentRootContainer().getWorlds())
                .filter(world -> world.getDimId().equals(dimensionKey))
                .findFirst()
                .orElse(null);

        if (targetedWorld != null) return targetedWorld;

        var dimensionDirectory = minimapSession.getDimensionHelper().getDimensionDirectoryName(dimensionKey);
        var worldNode = minimapSession.getWorldStateUpdater().getPotentialWorldNode(dimensionKey, true);

        var worldPath = minimapSession.getWorldState()
                .getAutoRootContainerPath()
                .resolve(dimensionDirectory)
                .resolve(worldNode);

        return manager.getWorld(worldPath);
    }

    public static Stream<Collection<Waypoint>> getAllWaypoints() {
        var minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();

        if (minimapSession == null) return Stream.of();

        var rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();

        var sets = Stream.<WaypointSet>of();

        for (var world : rootContainer.getWorlds()) {
            sets = Stream.concat(sets, toStream(world.getIterableWaypointSets()));
        }

        for (var container : rootContainer.getSubContainers()) {
            for (var world : container.getWorlds()) {
                sets = Stream.concat(sets, toStream(world.getIterableWaypointSets()));
            }
        }

        return sets.map(waypointSet -> {
            var waypoints = waypointSet.getWaypoints();

            return (waypoints instanceof Collection<Waypoint> collection) ? collection : null;
        }).filter(Objects::nonNull);
    }

    public static <E> Stream<E> toStream(final Iterable<E> iterable) {
        return iterable == null ? Stream.empty() : StreamSupport.stream(iterable.spliterator(), false);
    }

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

    public static Collection<WaystonePosition> getClientValidDiscovered() {
        var data = getPlayerData();
        var storage = getStorage();

        if (data != null && storage != null) {
            var discoveredWaystones = data.discoveredWaystones();

            return discoveredWaystones.stream()
                    .map(storage::getPosition)
                    .filter(Objects::nonNull)
                    .toList();
        }

        return Collections.emptyList();
    }
}
