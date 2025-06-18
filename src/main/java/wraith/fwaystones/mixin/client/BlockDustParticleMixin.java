package wraith.fwaystones.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.util.pond.BakedModelParticleEffectExtension;

@Mixin(BlockDustParticle.class)
public abstract class BlockDustParticleMixin {

    @WrapOperation(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
    private Sprite fwaystones$adjustParticle(BlockModels instance, BlockState state, Operation<Sprite> original, @Local(argsOnly = true) ClientWorld world, @Local(argsOnly = true)BlockPos pos){
        return (instance.getModel(state) instanceof BakedModelParticleEffectExtension effectExtension)
                ? effectExtension.getParticleState(world, pos, state)
                : original.call(instance, state);
    }
}
