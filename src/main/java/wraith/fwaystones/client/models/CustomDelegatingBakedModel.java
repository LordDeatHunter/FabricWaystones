package wraith.fwaystones.client.models;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.util.pond.BakedModelParticleEffectExtension;

import java.util.*;
import java.util.function.Supplier;

public class CustomDelegatingBakedModel<E> implements BakedModel, BakedModelParticleEffectExtension {

    private final BakedModel model;
    private final ExtraModelDataGetter<E> extraDataGetter;
    private final QuadEmission<E> emissionCallback;

    public CustomDelegatingBakedModel(BakedModel model, ExtraModelDataGetter<E> extraDataGetter, QuadEmission<E> emissionCallback) {
        this.model = model;
        this.extraDataGetter = extraDataGetter;
        this.emissionCallback = emissionCallback;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return getModel().getQuads(state, face, random);
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        var extraData = this.extraDataGetter.getData(blockView, pos, state);

        if (extraData != null) {
            emissionCallback.emitBlockQuads(blockView, state, pos, extraData, model, randomSupplier, context);
        }
    }

    private BakedModel getModel() {
        return model;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return getModel().useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return getModel().hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return getModel().isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return getModel().isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return getModel().getParticleSprite();
    }

    @Override
    public Sprite getParticleSprite(BlockRenderView world, BlockPos pos, BlockState state) {
        var e = this.extraDataGetter.getData(world, pos, state);

        if (e != null){
            return this.emissionCallback.getParticleSprite(world, pos, state, e);
        }

        return MinecraftClient.getInstance().getBakedModelManager().getMissingModel().getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return getModel().getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return getModel().getOverrides();
    }

}
