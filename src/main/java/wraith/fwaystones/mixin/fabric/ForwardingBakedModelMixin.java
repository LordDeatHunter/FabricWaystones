package wraith.fwaystones.mixin.fabric;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import wraith.fwaystones.pond.BakedModelParticleEffectExtension;

@Mixin(ForwardingBakedModel.class)
public abstract class ForwardingBakedModelMixin implements BakedModelParticleEffectExtension {
    @Shadow protected BakedModel wrapped;

    @Override
    @Nullable
    public Sprite getParticleSpriteFromWorld(BlockRenderView blockView, BlockPos pos, BlockState state) {
        if (this.wrapped instanceof BakedModelParticleEffectExtension extension) {
            return extension.getParticleSpriteFromWorld(blockView, pos, state);
        }

        return null;
    }
}
