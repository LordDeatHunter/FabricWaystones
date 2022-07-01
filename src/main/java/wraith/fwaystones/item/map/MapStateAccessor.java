package wraith.fwaystones.item.map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface MapStateAccessor {
    boolean addWaystone(WorldAccess world, BlockPos pos);
    @Nullable BlockPos removeWaystone(BlockView world, int x, int z);

    Collection<MapWaystoneMarker> getFwaystones$waystones();
}
