package wraith.fwaystones.item.map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public interface MapStateAccessor {
    boolean addWaystone(WorldAccess world, BlockPos pos);
}
