package wraith.fwaystones.client.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import wraith.fwaystones.FabricWaystones;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class DynamicModelUtils {

    public static JsonObject createOverridenItemModel(List<Identifier> textureLayers, Stream<Identifier> entries, Identifier predicateKey) {
        var obj = createItemModel(textureLayers);
        obj.add("overrides", getOverrides(entries, predicateKey));

        return obj;
    }

    public static JsonObject createItemModel(List<Identifier> textureLayers) {
        var obj = new JsonObject();
        obj.addProperty("parent", "item/generated");
        addItemTextures(obj, textureLayers);
        return obj;
    }

    private static void addItemTextures(JsonObject modelObj, List<Identifier> textureLayers) {
        addTextures(modelObj, Util.make(new LinkedHashMap<>(), map -> {
            for (int i = 0; i < textureLayers.size(); i++) {
                var layer = textureLayers.get(i);

                map.put("layer" + i, layer);
            }
        }));
    }

    public static JsonArray getOverrides(Stream<Identifier> entries, Identifier predicateKey) {
        return getOverrides(entries, predicateKey.toString(), Identifier::toString);
    }

    public static <T> JsonArray getOverrides(Stream<T> entries, String predicateKey, Function<T, String> toModel) {
        var index = 1;

        var array = new JsonArray();

        for (var entry : entries.toList()) {
            array.add(getOverride(index, entry, predicateKey, toModel));

            index++;
        }

        return array;
    }

    public static <T> JsonObject getOverride(int index, T entry, String predicateKey, Function<T, String> toModel) {
        var obj = new JsonObject();

        obj.add("predicate", Util.make(new JsonObject(), predicate -> {
            predicate.addProperty(predicateKey, index);
        }));
        obj.addProperty("model", toModel.apply(entry));

        return obj;
    }

    //--

    public static JsonObject createBlockModel(Identifier parentModel, Identifier particleId, Consumer<Map<String, Identifier>> texturesBuilder) {
        return createBlockModel(parentModel, map -> {
            map.put("particle", particleId);
            texturesBuilder.accept(map);
        });
    }

    public static JsonObject createBlockModel(Identifier parentModel, Consumer<Map<String, Identifier>> texturesBuilder) {
        var obj = new JsonObject();
        obj.addProperty("parent", parentModel.toString());
        addTextures(obj, Util.make(new LinkedHashMap<>(), texturesBuilder));

        return obj;
    }

    //--

    private static void addTextures(JsonObject modelObj, Map<String, Identifier> textures) {
        var obj = new JsonObject();
        for (var entry : textures.entrySet()) {
            obj.addProperty(entry.getKey(), entry.getValue().toString());
        }
        modelObj.add("textures", obj);
    }
}
