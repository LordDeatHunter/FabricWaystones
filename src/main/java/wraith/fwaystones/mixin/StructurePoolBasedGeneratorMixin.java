package wraith.fwaystones.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.StructurePoolBasedGenerator_StructurePoolGeneratorAccess;
import wraith.fwaystones.util.Utils;

import java.util.List;

@Mixin(StructurePoolBasedGenerator.class)
public class StructurePoolBasedGeneratorMixin {

    @Inject(method = "generate(Lnet/minecraft/world/gen/noise/NoiseConfig;IZLnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/world/HeightLimitView;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/registry/Registry;Lnet/minecraft/structure/PoolStructurePiece;Ljava/util/List;Lnet/minecraft/util/shape/VoxelShape;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;addLast(Ljava/lang/Object;)V"),
        locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void preGenerate2(NoiseConfig noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, HeightLimitView heightLimitView, Random random, Registry<StructurePool> structurePoolRegistry, PoolStructurePiece firstPiece, List<PoolStructurePiece> pieces, VoxelShape pieceShape, CallbackInfo ci, StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator) {
        onInit(structurePoolGenerator);
    }

    @Unique
    private static void onInit(StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator) {
        var config = FabricWaystones.CONFIG.worldgen;
        int maxWaystoneCount = Utils.getRandomIntInRange(config.min_per_village(), config.max_per_village());
        ((StructurePoolBasedGenerator_StructurePoolGeneratorAccess) (Object) structurePoolGenerator).setMaxWaystoneCount(maxWaystoneCount);
    }

}
