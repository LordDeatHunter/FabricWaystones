package wraith.fwaystones.access;

import net.minecraft.util.math.BlockPos;
import wraith.fwaystones.block.WaystoneBlockEntity;

public interface WaystoneValue {

    WaystoneBlockEntity getEntity();

    String getWaystoneName();

    BlockPos way_getPos();

    String getWorldName();

    boolean isGlobal();

    String getHash();

    int getColor();

    void setColor(int color);
}
