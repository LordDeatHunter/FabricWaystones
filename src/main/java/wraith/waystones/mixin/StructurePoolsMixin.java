/*


-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
||         Only works with vanilla villages.         ||
-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-


package wraith.waystones.mixin;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.waystones.utils.Utils;


@Mixin(StructurePools.class)
class StructurePoolsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void inject(StructurePool spool, CallbackInfoReturnable<StructurePool> cir) {
        StructurePool pool = spool;

        pool = Utils.tryAddElementToPool(new Identifier("village/plains/houses"), pool, "waystones:village_waystone", StructurePool.Projection.RIGID, 2);
        pool = Utils.tryAddElementToPool(new Identifier("village/desert/houses"), pool, "waystones:village_waystone", StructurePool.Projection.RIGID, 2);
        pool = Utils.tryAddElementToPool(new Identifier("village/savanna/houses"), pool, "waystones:village_waystone", StructurePool.Projection.RIGID, 2);
        pool = Utils.tryAddElementToPool(new Identifier("village/taiga/houses"), pool, "waystones:village_waystone", StructurePool.Projection.RIGID, 2);
        pool = Utils.tryAddElementToPool(new Identifier("village/snowy/houses"), pool, "waystones:village_waystone", StructurePool.Projection.RIGID, 2);

        cir.setReturnValue(BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_POOL, pool.getId(), pool));
        cir.cancel();
    }
}
 */