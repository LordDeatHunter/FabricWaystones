package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(JigsawPlacement.Placer.class)
public class StructurePoolBasedGenerator_StructurePoolGeneratorMixin implements StructurePoolBasedGenerator_StructurePoolGeneratorAccess {

    @Shadow
    @Final
    private List<? super PoolElementStructurePiece> pieces;

    @Unique
    private int maxWaystoneCount = -1;

    @Unique
    private static boolean isWaystone(StructurePoolElement element) {
        return element instanceof SinglePoolElement singlePoolElement
            && ((SinglePoolElementAccessor) singlePoolElement)
            .getLocation()
            .left()
            .orElse(ResourceLocation.parse("empty"))
            .getNamespace()
            .equals(FabricWaystones.MOD_ID);
    }

    @Unique
    public void fabricWaystones$setMaxWaystoneCount(int maxWaystoneCount) {
        this.maxWaystoneCount = maxWaystoneCount;
    }

    @Inject(method = "tryPlacingChildren",
        at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", ordinal = 0, shift = At.Shift.AFTER, remap = false))
    private void fabricwaystones_limitWaystonePieceSpawning(PoolElementStructurePiece piece,
                                                            MutableObject<VoxelShape> pieceShape,
                                                            int depth,
                                                            boolean modifyBoundingBox,
                                                            LevelHeightAccessor world,
                                                            RandomState noiseConfig,
                                                            PoolAliasLookup aliasLookup,
                                                            LiquidSettings liquidSettings,
                                                            CallbackInfo ci,
                                                            @Local ResourceKey<StructureTemplatePool> registryKey,
                                                            @Local List<StructurePoolElement> list
    ) {
        if (!FabricWaystones.CONFIG.worldgen.generate_in_villages() ||
            maxWaystoneCount < 0 ||
            !WaystonesWorldgen.VANILLA_VILLAGES.containsKey(registryKey.location())) {
            return;
        }
        long villageWaystoneCount = pieces.stream()
            .filter(element -> element instanceof PoolElementStructurePiece poolStructurePiece
                && poolStructurePiece.getElement() instanceof SinglePoolElement singlePoolElement
                && ((SinglePoolElementAccessor) singlePoolElement)
                .getLocation()
                .left()
                .orElse(ResourceLocation.parse("empty"))
                .getNamespace()
                .equals(FabricWaystones.MOD_ID)
            )
            .count();
        final boolean hasMaxWaystones = villageWaystoneCount >= maxWaystoneCount;
        list.removeIf(element -> hasMaxWaystones && isWaystone(element));
    }
}
