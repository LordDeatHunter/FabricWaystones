package wraith.fwaystones.util.pond;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.joml.Vector3f;

public interface BakedModelParticleEffectExtension {
    Sprite getParticleState(BlockRenderView blockView, BlockPos pos, BlockState state);
}
