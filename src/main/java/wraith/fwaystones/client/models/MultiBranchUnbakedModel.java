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
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Function;

public class MultiBranchUnbakedModel<K extends Record> implements UnbakedModel {

    private final SequencedMap<K, UnbakedModel> keyToModel;
    private final BranchKeyGetter<K> branchKeyGetter;

    public MultiBranchUnbakedModel(SequencedMap<K, UnbakedModel> typeToModel, BranchKeyGetter<K> branchKeyGetter) {
        this.keyToModel = typeToModel;
        this.branchKeyGetter = branchKeyGetter;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return keyToModel.values().stream().flatMap(unbakedModel -> unbakedModel.getModelDependencies().stream()).toList();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
        keyToModel.values().forEach(unbakedModel -> unbakedModel.setParents(modelLoader));
    }

    @Override
    public @Nullable BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
        var keyToModel = new LinkedHashMap<K, BakedModel>();

        this.keyToModel.forEach((identifier, unbakedModel) -> {
            keyToModel.put(identifier, unbakedModel.bake(baker, textureGetter, rotationContainer));
        });

        return MultiBranchBakedModel.of(keyToModel, branchKeyGetter);
    }
}
