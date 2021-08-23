package wraith.waystones;

import wraith.waystones.block.WaystoneBlockEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface WaystoneValue {
	WaystoneBlockEntity getEntity();

	String getWaystoneName();

	BlockPos way_getPos();

	String getWorldName();

	boolean isGlobal();
}
