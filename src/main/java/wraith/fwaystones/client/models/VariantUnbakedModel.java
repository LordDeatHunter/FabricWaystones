package wraith.fwaystones.client.models;

import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class VariantUnbakedModel implements UnbakedModel {

    private final ModelVariant variant;

    public VariantUnbakedModel(Identifier location, int y) {
        this(location, 0, y, false, 1);
    }

    public VariantUnbakedModel(Identifier location, int x, int y, boolean uvLock, int weight) {
        this(new ModelVariant(location, ModelRotation.get(x, y).getRotation(), uvLock, weight));
    }

    public VariantUnbakedModel(ModelVariant variant) {
        this.variant = variant;
    }

    public boolean equals(Object o) {
        return this == o
                || (o instanceof VariantUnbakedModel variantUnbakedModel && this.variant.equals(variantUnbakedModel.variant));
    }

    public int hashCode() {
        return this.variant.hashCode();
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.singleton(variant.getLocation());
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        modelLoader.apply(variant.getLocation()).setParents(modelLoader);
    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        return baker.bake(variant.getLocation(), variant);
    }
}
