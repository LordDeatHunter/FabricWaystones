package wraith.fwaystones.mixin.client;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelPredicateProviderRegistry.class)
public interface ModelPredicateProviderRegistryAccessor {
    @Accessor("GLOBAL")
    static Map<Identifier, ModelPredicateProvider> fwaystones$GLOBAL() {
        throw new IllegalStateException("UHHHHHHHHHHHHHH");
    }
}
