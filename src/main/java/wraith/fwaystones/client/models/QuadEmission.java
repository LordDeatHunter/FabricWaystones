package wraith.fwaystones.client.models;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

public interface QuadEmission<E> {
    void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, E extraData, BakedModel model, Supplier<Random> randomSupplier, RenderContext context);

    Sprite getParticleSprite(BlockRenderView world, BlockPos pos, BlockState state, E extraData);
}
