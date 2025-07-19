package wraith.fwaystones.registry;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.components.*;

import java.util.function.UnaryOperator;

public class WaystoneDataComponents {
    public static final ComponentType<WaystoneHashTargets> HASH_TARGETS = register("hash_targets", builder -> builder.endec(WaystoneHashTargets.ENDEC));

    public static final ComponentType<WaystoneHashTarget> HASH_TARGET = register("hash_target", builder -> builder.endec(WaystoneHashTarget.ENDEC));

    public static final ComponentType<WaystoneDataHolder> DATA_HOLDER = register("data_holder", builder -> builder.endec(WaystoneDataHolder.ENDEC));

    public static final ComponentType<InfiniteKnowledge> HAS_INFINITE_KNOWLEDGE = register("has_infinite_knowledge", builder -> builder.endec(InfiniteKnowledge.ENDEC));

    public static final ComponentType<WaystoneTeleporter> TELEPORTER = register("waystone_teleporter", builder -> builder.endec(WaystoneTeleporter.ENDEC));

    public static final ComponentType<WaystoneTyped> WAYSTONE_TYPE = register("waystone_type", builder -> builder.endec(WaystoneTyped.ENDEC));

    public static void init() {}

    private static <T> ComponentType<T> register(String path, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, FabricWaystones.id(path), ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build());
    }
}
