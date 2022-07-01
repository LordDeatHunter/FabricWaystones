package wraith.fwaystones.item.map;

import net.minecraft.item.map.MapIcon;

public interface MapIconTypeAccessor {
    MapIcon.Type setIsWaystone(boolean bl);
    boolean getIsWaystone();
}
