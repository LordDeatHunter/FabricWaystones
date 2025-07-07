package wraith.fwaystones.pond;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public interface BakedModelParticleEffectExtension {
    default @Nullable Sprite getParticleSpriteFromWorld(BlockRenderView blockView, BlockPos pos, BlockState state) {
        return null;
    }
}
