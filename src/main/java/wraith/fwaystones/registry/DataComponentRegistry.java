package wraith.fwaystones.registry;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.function.UnaryOperator;

public final class DataComponentRegistry {

    public static ComponentType<List<String>> WAYSTONES;
    public static ComponentType<String> BOUND_WAYSTONE;

    private DataComponentRegistry() {}

    public static void init() {
        WAYSTONES = register(Identifier.of("fwaystones", "waystones"), (builder) -> builder.codec(Codec.list(Codec.STRING)));
        BOUND_WAYSTONE = register(Identifier.of("fwaystones", "bound_waystone"), (builder) -> builder.codec(Codec.STRING));
    }

    private static <T> ComponentType<T> register(Identifier id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
    }
}
