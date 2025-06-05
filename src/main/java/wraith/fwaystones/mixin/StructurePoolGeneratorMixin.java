package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.util.pond.StructurePoolGeneratorExtension;
import wraith.fwaystones.registry.WaystonesWorldgen;

import java.util.Collection;
import java.util.List;

@Mixin(StructurePoolBasedGenerator.StructurePoolGenerator.class)
public abstract class StructurePoolGeneratorMixin implements StructurePoolGeneratorExtension {

    @Shadow
    @Final
    private List<? super PoolStructurePiece> children;

    @Unique
    private int maxWaystoneCount = -1;

    @Unique
    private static boolean isWaystone(StructurePoolElement element) {
        return element instanceof SinglePoolElement singlePoolElement && isWaystonePoolElement(singlePoolElement);
    }

    @Unique
    private static boolean isWaystonePoolElement(SinglePoolElement element) {
        var namespace = ((SinglePoolElementAccessor) element).getLocation()
                .left()
                .map(Identifier::getNamespace)
                .orElse("invalid");

        return namespace.equals(FabricWaystones.MOD_ID);
    }

    @Unique
    public void fwaystone$setMaxWaystoneCount(int maxWaystoneCount) {
        this.maxWaystoneCount = maxWaystoneCount;
    }

    @WrapOperation(
            method = "generatePiece",
            at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", ordinal = 0))
    private boolean test(List<StructurePoolElement> list, Collection<? extends StructurePoolElement> es, Operation<Boolean> original, @Local(ordinal = 0) RegistryKey<StructurePool> registryKey) {
        var result = original.call(list, es);

        if (maxWaystoneCount > 0 && WaystonesWorldgen.VANILLA_VILLAGES.containsKey(registryKey.getValue())) {
            var villageWaystoneCount = 0;

            for (var piece : children) {
                if (piece instanceof PoolStructurePiece poolPiece && poolPiece.getPoolElement() instanceof SinglePoolElement element && isWaystonePoolElement(element)) {
                    villageWaystoneCount++;
                }
            }

            var hasMaxWaystones = villageWaystoneCount >= maxWaystoneCount;

            list.removeIf(element -> hasMaxWaystones && isWaystone(element));
        }

        return result;
    }
}
