package wraith.waystones.access;

import wraith.waystones.block.WaystoneBlockEntity;

import net.minecraft.util.math.BlockPos;

public interface WaystoneValue {
	WaystoneBlockEntity getEntity();

	String getWaystoneName();

	BlockPos way_getPos();

	String getWorldName();

	boolean isGlobal();

	String getHash();
}
