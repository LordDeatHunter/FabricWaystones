package wraith.fwaystones.mixin.client;

import net.minecraft.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.pond.BakedModelParticleEffectExtension;

@Mixin(BakedModel.class)
public interface BakedModelMixin extends BakedModelParticleEffectExtension {
}
