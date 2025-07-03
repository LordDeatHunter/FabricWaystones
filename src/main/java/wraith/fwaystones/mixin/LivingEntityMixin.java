package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.pond.BlockStateParticleEffectExtension;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(method = "tryUseTotem")
    public boolean revive(DamageSource source, Operation<Boolean> original) {
        if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            var stack = WaystoneComponentEventHooks.getVoidTotem(player);

            if (stack != null && WaystoneComponentEventHooks.attemptVoidTotemEffects(player, stack, source)) {
                return true;
            }
        }

        return original.call(source);
    }

    @WrapOperation(method = "fall", at = @At(value = "NEW", target = "(Lnet/minecraft/particle/ParticleType;Lnet/minecraft/block/BlockState;)Lnet/minecraft/particle/BlockStateParticleEffect;"))
    private BlockStateParticleEffect addTruePos(ParticleType type, BlockState blockState, Operation<BlockStateParticleEffect> original, @Local(ordinal = 0, argsOnly = true) BlockPos blockPos){
        var result = original.call(type, blockState);

        if (result instanceof BlockStateParticleEffectExtension extension) {
            extension.fwaystones$setTruePos(blockPos);
        }

        return result;
    }
}
