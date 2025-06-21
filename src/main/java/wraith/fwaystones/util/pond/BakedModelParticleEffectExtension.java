package wraith.fwaystones.util.pond;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public interface BakedModelParticleEffectExtension {
    Sprite getParticleSprite(BlockRenderView blockView, BlockPos pos, BlockState state);
}
