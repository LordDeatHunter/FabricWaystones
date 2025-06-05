package wraith.fwaystones.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;

import java.util.function.Supplier;

public final class WaystoneCompatEntries {

    private WaystoneCompatEntries() {}

    @SuppressWarnings("unchecked")
    public static void init() {
        Registries.REGISTRIES.getOrEmpty(Identifier.of("repurposed_structures", "json_conditions"))
            .ifPresent(registry -> Registry.register(
                (Registry<Supplier<Boolean>>) registry,
                    FabricWaystones.id("config"),
                FabricWaystones.CONFIG.worldgen::generate_in_villages
            ));
    }

}
