package wraith.fwaystones.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.Utils;

import java.util.function.Supplier;

public final class CompatRegistry {

    private CompatRegistry() {}

    @SuppressWarnings("unchecked")
    public static void init() {
        Registries.REGISTRIES.getOrEmpty(new Identifier("repurposed_structures", "json_conditions"))
            .ifPresent(registry -> Registry.register(
                (Registry<Supplier<Boolean>>) registry,
                Utils.ID("config"),
                FabricWaystones.CONFIG.worldgen::generate_in_villages
            ));
    }

}
