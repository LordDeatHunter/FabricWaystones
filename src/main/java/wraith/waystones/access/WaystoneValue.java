package wraith.waystones.access;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.block.WaystoneBlockEntity;

public interface WaystoneValue {

    WaystoneBlockEntity getEntity();

    String getWaystoneName();

    BlockPos way_getPos();

    String getWorldName();

    void setColor(@Nullable Integer color);

    boolean isGlobal();

    String getHash();

    @Nullable Integer getColor();
}
