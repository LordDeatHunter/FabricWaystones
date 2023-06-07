package wraith.fwaystones.mixin;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.StructurePoolBasedGenerator_StructurePoolGeneratorAccess;
import wraith.fwaystones.util.WaystonesWorldgen;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
                .orElse(new ResourceLocation("empty"))
                .getNamespace()
                .equals(Waystones.MOD_ID);
    }

    @Unique
    public void setMaxWaystoneCount(int maxWaystoneCount) {
        this.maxWaystoneCount = maxWaystoneCount;
    }

    @Unique
    private static boolean limitWaystonePieceSpawning(List<? super PoolElementStructurePiece> pieces, int maxWaystoneCount, StructurePoolElement element){
        final boolean hasMaxWaystones = pieces.stream()
                .filter(element2 -> element2 instanceof PoolElementStructurePiece poolStructurePiece
                        && poolStructurePiece.getElement() instanceof SinglePoolElement singlePoolElement
                        && ((SinglePoolElementAccessor) singlePoolElement)
                        .getLocation()
                        .left()
                        .orElse(new ResourceLocation("empty"))
                        .getNamespace()
                        .equals(Waystones.MOD_ID)
                )
                .count() >= maxWaystoneCount;
        return hasMaxWaystones == isWaystone(element);
    }


    @Inject(method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;)V", at = @At(value = "HEAD"))
    private void forgewaystones_startGeneratePiece(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int i, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState, CallbackInfo ci) {}

    @PlatformOnly("forge")
    @Inject(method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", ordinal = 0, shift = At.Shift.AFTER, remap = false),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void forgewaystones_limitWaystonePieceSpawning(
            PoolElementStructurePiece poolElementStructurePiece,
            MutableObject<VoxelShape> mutableObject,
            int i,
            boolean bl,
            LevelHeightAccessor levelHeightAccessor,
            RandomState randomState,
            CallbackInfo ci,
            StructurePoolElement structurePoolElement,
            BlockPos blockPos,
            Rotation rotation,
            StructureTemplatePool.Projection projection,
            boolean bl2,
            MutableObject<VoxelShape> mutableObject2,
            BoundingBox boundingBox,
            int j,
            Iterator var15,
            StructureTemplate.StructureBlockInfo structureBlockInfo,
            Direction direction,
            BlockPos blockPos2,
            BlockPos blockPos3,
            int k,
            int l,
            ResourceLocation resourceLocation,
            Optional<StructureTemplatePool> optional,
            ResourceLocation resourceLocation2,
            Optional<StructureTemplatePool> optional2,
            MutableObject<Object> mutableObject3,
            boolean bl3,
            List<StructurePoolElement> list
    ) {
        if (!Waystones.CONFIG.worldgen.generate_in_villages ||
                maxWaystoneCount < 0 ||
                !WaystonesWorldgen.VANILLA_VILLAGES.containsKey(resourceLocation)) {
            return;
        }
        list.removeIf(element -> limitWaystonePieceSpawning(pieces, maxWaystoneCount, element));
    }

    @SuppressWarnings("all")
    @PlatformOnly("fabric")
    @Inject(method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", ordinal = 0, shift = At.Shift.AFTER, remap = false),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void fabricwaystones_limitWaystonePieceSpawning(
            PoolElementStructurePiece arg,
            MutableObject<VoxelShape> mutableObject,
            int i,
            boolean bl,
            LevelHeightAccessor arg2,
            RandomState arg3,
            CallbackInfo ci,
            StructurePoolElement structurePoolElement,
            BlockPos blockPos,
            Rotation rotation,
            StructureTemplatePool.Projection projection,
            boolean bl2,
            MutableObject<VoxelShape> mutableObject2,
            BoundingBox boundingBox,
            int j,
            Iterator var15,
            StructureTemplate.StructureBlockInfo structureBlockInfo2,
            Direction direction,
            BlockPos blockPos2,
            BlockPos blockPos3,
            int k,
            int l,
            ResourceLocation resourceLocation,
            Optional<StructureTemplatePool> optional,
            ResourceLocation resourceLocation2,
            Optional<StructureTemplatePool> optional2,
            MutableObject<Object> mutableObject3,
            //boolean bl3, WARNING: REMOVED FOR FABRIC COMPAT
            List<StructurePoolElement> list
    ) {
        if (!Waystones.CONFIG.worldgen.generate_in_villages ||
                maxWaystoneCount < 0 ||
                !WaystonesWorldgen.VANILLA_VILLAGES.containsKey(resourceLocation)) {
            return;
        }
        list.removeIf(element -> limitWaystonePieceSpawning(pieces, maxWaystoneCount, element));
    }












}
