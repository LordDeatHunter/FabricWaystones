package wraith.fwaystones.pond;

import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.List;
import java.util.UUID;

public interface MapStateDuck {
    List<UUID> fwaystones$getWaystoneMarkers();

    boolean fwaystones$toggleWaystoneMarker(WaystoneData waystoneData, WaystonePosition position);

    void fwaystones$setWaystoneMarkers(List<UUID> markers);
}
