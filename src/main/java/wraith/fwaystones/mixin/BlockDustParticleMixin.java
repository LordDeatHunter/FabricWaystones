package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.pond.BlockStateParticleEffectExtension;

@Mixin(BlockDustParticle.class)
public abstract class BlockDustParticleMixin {
    @WrapOperation(method = "create", at = @At(value = "NEW", target = "(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;)Lnet/minecraft/client/particle/BlockDustParticle;"))
    private static BlockDustParticle useTruePosIfPossible(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, Operation<BlockDustParticle> original, @Local(argsOnly = true) BlockStateParticleEffect parameters) {
        if (parameters instanceof BlockStateParticleEffectExtension extension) {
            var truePos = extension.fwaystones$getTruePos();

            if (truePos != null) {
                return new BlockDustParticle(world, x, y, z, velocityX, velocityY, velocityZ, state, truePos);
            }
        }

        return original.call(world, x, y, z, velocityX, velocityY, velocityZ, state);
    }
}
