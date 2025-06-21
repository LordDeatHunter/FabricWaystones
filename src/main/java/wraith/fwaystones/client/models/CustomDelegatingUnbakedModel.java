package wraith.fwaystones.client.models;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public class CustomDelegatingUnbakedModel<E> implements UnbakedModel {

    private final UnbakedModel unbakedModel;
    private final ExtraModelDataGetter<E> extraDataGetter;
    private final QuadEmission<E> emissionCallback;

    public CustomDelegatingUnbakedModel(UnbakedModel unbakedModel, ExtraModelDataGetter<E> extraDataGetter, QuadEmission<E> emissionCallback) {
        this.unbakedModel = unbakedModel;
        this.extraDataGetter = extraDataGetter;
        this.emissionCallback = emissionCallback;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return unbakedModel.getModelDependencies();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        unbakedModel.setParents(modelLoader);
    }

    @Override
    public @Nullable BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        return new CustomDelegatingBakedModel<E>(unbakedModel.bake(baker, textureGetter, rotationContainer), this.extraDataGetter, this.emissionCallback);
    }
}
