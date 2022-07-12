package wraith.fwaystones.item.map;

import net.minecraft.item.map.MapIcon;

public interface MapIconAccessor {
    MapIcon setIsWaystone(boolean bl);
    boolean getIsWaystone();
}
