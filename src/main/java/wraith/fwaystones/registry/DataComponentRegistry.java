package wraith.fwaystones.registry;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class DataComponentRegistry {

    public static DataComponentType<List<String>> WAYSTONES;
    public static DataComponentType<String> BOUND_WAYSTONE;

    private DataComponentRegistry() {}

    public static void init() {
        WAYSTONES = register(ResourceLocation.fromNamespaceAndPath("fwaystones", "waystones"), (builder) -> builder.persistent(Codec.list(Codec.STRING)));
        BOUND_WAYSTONE = register(ResourceLocation.fromNamespaceAndPath("fwaystones", "bound_waystone"), (builder) -> builder.persistent(Codec.STRING));
    }

    private static <T> DataComponentType<T> register(ResourceLocation id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, (builderOperator.apply(DataComponentType.builder())).build());
    }
}
