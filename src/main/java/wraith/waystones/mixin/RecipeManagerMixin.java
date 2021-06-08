package wraith.waystones.mixin;

import com.google.gson.JsonElement;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import wraith.waystones.Config;
import wraith.waystones.Utils;
import wraith.waystones.Waystones;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply", at = @At("HEAD"))
    public void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        HashMap<String, JsonElement> recipes = Config.getInstance().getRecipes();
        for (Map.Entry<String, JsonElement> recipe : recipes.entrySet()) {
            map.put(Utils.ID(recipe.getKey()), recipe.getValue());
        }
    }

}