package wraith.waystones.mixin;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.waystones.Config;
import wraith.waystones.Utils;

import java.util.Map;

@Mixin(DynamicRegistryManager.class)
public abstract class DynamicRegistryManagerMixin {

    @Inject(method = "load(Lnet/minecraft/util/dynamic/RegistryOps;Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/util/registry/DynamicRegistryManager$Info;)V", at = @At(value = "TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private static <E> void load(RegistryOps<?> ops, DynamicRegistryManager dynamicRegistryManager, DynamicRegistryManager.Info<E> info, CallbackInfo ci, RegistryKey<? extends Registry<E>> registryKey, SimpleRegistry<E> simpleRegistry) {
        if (!Config.getInstance().generateInVillages()) {
            return;
        }
        if(registryKey.getValue().toString().contains("template_pool")) {
            for (Map.Entry<RegistryKey<Object>, Object> e : ((SimpleRegistryAccessor)simpleRegistry).getKeyToEntry().entrySet()) {
                if (!(e.getValue() instanceof StructurePool)) {
                    continue;
                }
                e.setValue(Utils.tryAddElementToPool(new Identifier("village/plains/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("village/desert/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("village/savanna/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("village/taiga/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("village/snowy/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/badlands/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/birch/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/crimson/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/dark_forest/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/giant_taiga/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/jungle/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/mountains/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/swamp/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
                e.setValue(Utils.tryAddElementToPool(new Identifier("repurposed_structures", "village/warped/houses"), (StructurePool) e.getValue(), "waystones:village_waystone", StructurePool.Projection.RIGID));
            }
        }
    }

}
