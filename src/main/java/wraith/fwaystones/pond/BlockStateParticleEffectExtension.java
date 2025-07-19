package wraith.fwaystones.pond;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface BlockStateParticleEffectExtension {
    void fwaystones$setTruePos(BlockPos pos);

    @Nullable
    BlockPos fwaystones$getTruePos();
}
