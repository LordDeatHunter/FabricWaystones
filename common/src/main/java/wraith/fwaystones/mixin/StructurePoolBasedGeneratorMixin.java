package wraith.fwaystones.mixin;

import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.StructurePoolBasedGenerator_StructurePoolGeneratorAccess;
import wraith.fwaystones.util.Utils;

import java.util.List;

@Mixin(JigsawPlacement.class)
public class StructurePoolBasedGeneratorMixin {

    @Inject(method = "addPieces(Lnet/minecraft/world/level/levelgen/RandomState;IZLnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Ljava/util/List;Lnet/minecraft/world/phys/shapes/VoxelShape;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Deque;addLast(Ljava/lang/Object;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void preGenerate2(RandomState noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor heightLimitView, RandomSource random, Registry<StructureTemplatePool> structurePoolRegistry, PoolElementStructurePiece firstPiece, List<PoolElementStructurePiece> pieces, VoxelShape pieceShape, CallbackInfo ci, JigsawPlacement.Placer placer) {
        onInit(placer);
    }
    @Unique
    private static void onInit(JigsawPlacement.Placer placer) {
        var config = Waystones.CONFIG.worldgen;
        int maxWaystoneCount = Utils.getRandomIntInRange(config.min_per_village, config.max_per_village);
        ((StructurePoolBasedGenerator_StructurePoolGeneratorAccess) (Object) placer).setMaxWaystoneCount(maxWaystoneCount);
    }

}