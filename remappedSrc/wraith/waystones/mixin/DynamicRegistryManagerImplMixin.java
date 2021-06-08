package wraith.waystones.mixin;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistryManager.Impl.class)
public interface DynamicRegistryManagerImplMixin {

    @Accessor("registries")
    Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> getRegistries();
}
