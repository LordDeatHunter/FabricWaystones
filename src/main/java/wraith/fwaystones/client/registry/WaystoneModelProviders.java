package wraith.fwaystones.client.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.registry.WaystoneDataComponents;

@Environment(EnvType.CLIENT)
public final class WaystoneModelProviders {

    public static void register() {
        ModelPredicateProviderRegistry.register(FabricWaystones.id("has_learned"),
            (stack, world, entity, seed) -> {
                if (!stack.isEmpty()) {
                    if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
                        var targets = stack.get(WaystoneDataComponents.HASH_TARGETS);

                        return targets == null || targets.ids().isEmpty() ? 0 : 1;
                    } else if (stack.contains(WaystoneDataComponents.HASH_TARGET)) {
                        var target = stack.get(WaystoneDataComponents.HASH_TARGET);

                        return target == null ? 0 : 1;
                    }
                }

                return 0;
            }
        );
    }

}
