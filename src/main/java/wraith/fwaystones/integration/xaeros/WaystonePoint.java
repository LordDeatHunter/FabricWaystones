package wraith.fwaystones.integration.xaeros;

import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaystonePoint extends Waypoint {
    private static final Map<UUID, WaystonePoint> UUID_TO_POINT = new ConcurrentHashMap<>();
    private static final Map<WaystonePosition, WaystonePoint> POS_TO_POINT = new ConcurrentHashMap<>();


    private final WaystoneData waystoneData;
    private final WaystonePosition waystonePosition;
    private final WaypointSet waypointSet;

    public WaystonePoint(WaystoneData waystoneData, WaystonePosition position, WaypointSet waypointSet) {
        super(
            position.blockPos().getX(),
            position.blockPos().getY() + 1,
            position.blockPos().getZ(),
            waystoneData.nameAsString(),
            "",
            WaypointColor.PURPLE, //TODO: override this color with 0x5F3D75
            WaypointPurpose.NORMAL,
            true,
            true
        );
        this.waystoneData = waystoneData;
        this.waystonePosition = position;
        this.waypointSet = waypointSet;
        deletePoint(waystoneData.uuid());
        deletePointAt(position);
        UUID_TO_POINT.put(waystoneData.uuid(), this);
        POS_TO_POINT.put(position, this);
        waypointSet.add(this);
    }

    @Override
    public String getName() {
        return waystoneData.nameAsString();
    }

    public void delete() {
        waypointSet.remove(this);
        UUID_TO_POINT.remove(waystoneData.uuid(), this);
        POS_TO_POINT.remove(waystonePosition, this);
    }

    public static void deletePoint(UUID uuid) {
        WaystonePoint point = UUID_TO_POINT.remove(uuid);
        if (point != null) point.delete();
    }

    public static void deletePointAt(WaystonePosition position) {
        WaystonePoint point = POS_TO_POINT.remove(position);
        if (point != null) point.delete();
    }

    public static void deleteAll() {
        POS_TO_POINT.values().forEach(WaystonePoint::delete);
    }
}
