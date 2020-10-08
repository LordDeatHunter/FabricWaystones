package wraith.waystones.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor {

    @Accessor("keyToEntry")
    <T>
    BiMap<RegistryKey<T>, T> getKeyToEntry();

}
