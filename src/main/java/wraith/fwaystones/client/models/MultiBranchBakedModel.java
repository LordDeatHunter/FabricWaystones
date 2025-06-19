package wraith.fwaystones.client.models;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
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
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.util.pond.BakedModelParticleEffectExtension;

import java.util.*;
import java.util.function.Supplier;

public abstract class MultiBranchBakedModel<K extends Record> implements BakedModel, BakedModelParticleEffectExtension {

    private final SequencedMap<K, BakedModel> keyToModel;

    public MultiBranchBakedModel(SequencedMap<K, BakedModel> typeToModel) {
        this.keyToModel = typeToModel;
    }

    public static <K extends Record> MultiBranchBakedModel<K> of(SequencedMap<K, BakedModel> keyToModel, BranchKeyGetter<K> getter) {
        return new MultiBranchBakedModel<K>(keyToModel) {
            @Override
            public @Nullable K getKey(BlockRenderView blockView, BlockState state, BlockPos pos) {
                return getter.getKey(blockView, state, pos);
            }
        };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return getFirstModel().getQuads(state, face, random);
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        BakedModel modelToRender = null;

        var key = getKey(blockView, state, pos);

        if (key != null) {
            var model = keyToModel.get(key);

            if (model != null) {
                modelToRender = model;
            }
        }

        if (modelToRender == null) {
            modelToRender = getFirstModel();
        }

        modelToRender.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    @Nullable
    public abstract K getKey(BlockRenderView blockView, BlockState state, BlockPos pos);

    private BakedModel getFirstModel() {
        return this.keyToModel.sequencedValues().getFirst();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return getFirstModel().useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return getFirstModel().hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return getFirstModel().isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return getFirstModel().isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return getFirstModel().getParticleSprite();
    }

    @Override
    public Sprite getParticleState(BlockRenderView world, BlockPos pos, BlockState state) {
        var blockEntityPos = state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        var waystone = world.getBlockEntity(blockEntityPos, WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY);

        if (waystone.isPresent()) {
            var type = waystone.get().getWaystoneType();
            var id = WaystoneTypes.getIdOrDefault(type);

            var model = keyToModel.get(id);

            if (model != null) {
                return model.getParticleSprite();
            }
        }

        return getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return getFirstModel().getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return getFirstModel().getOverrides();
    }

}
