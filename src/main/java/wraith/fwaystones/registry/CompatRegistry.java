package wraith.fwaystones.registry;

import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.Utils;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class CompatRegistry {

    private CompatRegistry() {}

    @SuppressWarnings("unchecked")
    public static void init() {
        BuiltInRegistries.REGISTRY.get(ResourceLocation.fromNamespaceAndPath("repurposed_structures", "json_conditions"))
            .ifPresent(holder -> Registry.register(
                (Registry<Supplier<Boolean>>) holder.value(),
                Utils.ID("config"),
                FabricWaystones.CONFIG.worldgen::generate_in_villages
            ));
    }

}
