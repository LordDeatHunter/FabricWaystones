package wraith.fwaystones.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
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
import wraith.fwaystones.util.pond.StructurePoolGeneratorExtension;
import wraith.fwaystones.util.Utils;

import java.util.List;

@Mixin(StructurePoolBasedGenerator.class)
public abstract class StructurePoolBasedGeneratorMixin {

    @Inject(method = "generate(Lnet/minecraft/world/gen/noise/NoiseConfig;IZLnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureTemplateManager;Lnet/minecraft/world/HeightLimitView;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/registry/Registry;Lnet/minecraft/structure/PoolStructurePiece;Ljava/util/List;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;Lnet/minecraft/structure/StructureLiquidSettings;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/pool/StructurePoolBasedGenerator$StructurePoolGenerator;generatePiece(Lnet/minecraft/structure/PoolStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/HeightLimitView;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;Lnet/minecraft/structure/StructureLiquidSettings;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void preGenerate2(NoiseConfig noiseConfig, int maxSize, boolean modifyBoundingBox, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, HeightLimitView heightLimitView, Random random, Registry<StructurePool> structurePoolRegistry, PoolStructurePiece firstPiece, List<PoolStructurePiece> pieces, VoxelShape pieceShape, StructurePoolAliasLookup aliasLookup, StructureLiquidSettings liquidSettings, CallbackInfo ci, StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator) {
        var config = FabricWaystones.CONFIG;

        int maxWaystoneCount = -1;

        if (config.generateInVillages()) maxWaystoneCount = Utils.getRandomIntInRange(config.minPerVillage(), config.maxPerVillage());

        ((StructurePoolGeneratorExtension) (Object) structurePoolGenerator).fwaystone$setMaxWaystoneCount(maxWaystoneCount);
    }
}
