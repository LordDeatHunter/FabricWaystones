package wraith.fwaystones.integration.xaeros;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.integration.WaystoneWaypointMaker;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointVisibilityType;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class XaerosMinimapWaypointMaker extends WaystoneWaypointMaker<Waypoint> {

    public static final XaerosMinimapWaypointMaker INSTANCE = new XaerosMinimapWaypointMaker();

    private boolean useSeperateSet = false;
    private WaypointVisibilityType visibilityType = WaypointVisibilityType.LOCAL;

    private XaerosMinimapWaypointMaker() {
        super();
    }

    public void useSeperateSet(boolean waypointSet) {
        this.useSeperateSet = waypointSet;

        shouldResetAllWaypoints = true;
    }

    public void visibilityType(WaypointVisibilityType type) {
        this.visibilityType = type;

        shouldResetAllWaypoints = true;
    }

    @Override
    public boolean validToCreateWaypoints() {
        var minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) return false;

        var worldManager = minimapSession.getWorldManager();
        if (worldManager == null) return false;

        var currentWorld = worldManager.getCurrentWorld();
        return currentWorld != null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean displayWaypoints() {
        return true;
    }

    @Override
    @Nullable
    protected WaystonePosition deleteWaypointFromPosition(Waypoint point) {
        var position = super.deleteWaypointFromPosition(point);

        if(position != null) {
            getOrCreateSet(position).remove(point);
        }

        return position;
    }

    @Override
    public Identifier getId() {
        return FabricWaystones.id("xaeros");
    }

    @Override
    public Waypoint createWaypoint(WaystonePosition pos, WaystoneData data) {
        var waypoint = new Waypoint(
                pos.blockPos().getX(),
                pos.blockPos().getY() + 1,
                pos.blockPos().getZ(),
                data.nameAsString(),
                "",
                WaypointColor.PURPLE, //TODO: override this color with 0x5F3D75
                WaypointPurpose.NORMAL,
                true,
                true
        );

        getOrCreateSet(pos).add(waypoint);

        waypoint.setVisibility(visibilityType);

        return waypoint;
    }

    //--

    private WaypointSet getOrCreateSet(final WaystonePosition position) {
        var waypointWorld = getWorldOrThrow(position);

        return getOrCreateSet(waypointWorld, this.useSeperateSet ? "Wraith Waystones" : "gui.xaero_default");
    }

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

    public static <E> Stream<E> toStream(final Iterable<E> iterable) {
        return iterable == null ? Stream.empty() : StreamSupport.stream(iterable.spliterator(), false);
    }
}
