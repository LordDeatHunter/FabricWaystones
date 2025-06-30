package wraith.fwaystones.mixin;

import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wraith.fwaystones.pond.BlockStateParticleEffectExtension;

@Mixin(BlockStateParticleEffect.class)
public abstract class BlockStateParticleEffectMixin implements BlockStateParticleEffectExtension {

    @Unique
    public BlockPos truePos = null;

    @Override
    public void fwaystones$setTruePos(BlockPos pos) {
        truePos = pos;
    }

    @Override
    public BlockPos fwaystones$getTruePos() {
        return truePos;
    }
}
