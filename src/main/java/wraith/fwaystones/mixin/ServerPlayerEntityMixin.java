package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.pond.BlockStateParticleEffectExtension;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @WrapOperation(method = "handleFall", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
    private BlockStateParticleEffect addTruePos(ParticleType type, BlockState blockState, Operation<BlockStateParticleEffect> original, @Local(ordinal = 0) BlockPos blockPos){
        var result = original.call(type, blockState);

        if (result instanceof BlockStateParticleEffectExtension extension) {
            extension.fwaystones$setTruePos(blockPos);
        }

        return result;
    }
}
