package wraith.fwaystones.mixin;

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
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.StructurePoolBasedGenerator_StructurePoolGeneratorAccess;
import wraith.fwaystones.util.WaystonesWorldgen;
import java.util.List;

@Mixin(StructurePoolBasedGenerator.StructurePoolGenerator.class)
public class StructurePoolBasedGenerator_StructurePoolGeneratorMixin implements StructurePoolBasedGenerator_StructurePoolGeneratorAccess {

    @Shadow
    @Final
    private List<? super PoolStructurePiece> children;

    @Unique
    private int maxWaystoneCount = -1;

    @Unique
    private static boolean isWaystone(StructurePoolElement element) {
        return element instanceof SinglePoolElement singlePoolElement
            && ((SinglePoolElementAccessor) singlePoolElement)
            .getLocation()
            .left()
            .orElse(Identifier.of("empty"))
            .getNamespace()
            .equals(FabricWaystones.MOD_ID);
    }

    @Unique
    public void setMaxWaystoneCount(int maxWaystoneCount) {
        this.maxWaystoneCount = maxWaystoneCount;
    }

    @Inject(method = "generatePiece",
        at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", ordinal = 0, shift = At.Shift.AFTER, remap = false))
    private void fabricwaystones_limitWaystonePieceSpawning(PoolStructurePiece piece,
                                                            MutableObject<VoxelShape> pieceShape,
                                                            int depth,
                                                            boolean modifyBoundingBox,
                                                            HeightLimitView world,
                                                            NoiseConfig noiseConfig,
                                                            StructurePoolAliasLookup aliasLookup,
                                                            StructureLiquidSettings liquidSettings,
                                                            CallbackInfo ci,
                                                            @Local RegistryKey<StructurePool> registryKey,
                                                            @Local List<StructurePoolElement> list
    ) {
        if (!FabricWaystones.CONFIG.worldgen.generate_in_villages() ||
            maxWaystoneCount < 0 ||
            !WaystonesWorldgen.VANILLA_VILLAGES.containsKey(registryKey.getValue())) {
            return;
        }
        long villageWaystoneCount = children.stream()
            .filter(element -> element instanceof PoolStructurePiece poolStructurePiece
                && poolStructurePiece.getPoolElement() instanceof SinglePoolElement singlePoolElement
                && ((SinglePoolElementAccessor) singlePoolElement)
                .getLocation()
                .left()
                .orElse(Identifier.of("empty"))
                .getNamespace()
                .equals(FabricWaystones.MOD_ID)
            )
            .count();
        final boolean hasMaxWaystones = villageWaystoneCount >= maxWaystoneCount;
        list.removeIf(element -> hasMaxWaystones && isWaystone(element));
    }
}
