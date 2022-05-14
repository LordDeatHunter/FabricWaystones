package wraith.waystones.mixin;

import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.random.ChunkRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wraith.waystones.access.StructurePoolBasedGenerator_StructurePoolGeneratorAccess;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;

import java.util.List;

@Mixin(StructurePoolBasedGenerator.class)
public class StructurePoolBasedGeneratorMixin {

    @Inject(method = "method_39824(Lnet/minecraft/structure/PoolStructurePiece;Lnet/minecraft/world/gen/feature/StructurePoolFeatureConfig;IIILnet/minecraft/util/registry/Registry;Lnet/minecraft/structure/pool/StructurePoolBasedGenerator$PieceFactory;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/structure/StructureManager;Lnet/minecraft/world/gen/random/ChunkRandom;Lnet/minecraft/util/math/BlockBox;ZLnet/minecraft/world/HeightLimitView;Lnet/minecraft/structure/StructurePiecesCollector;Lnet/minecraft/structure/StructurePiecesGenerator$Context;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Deque;addLast(Ljava/lang/Object;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void preGenerate2(PoolStructurePiece poolStructurePiece, StructurePoolFeatureConfig structurePoolFeatureConfig, int i, int j, int k, Registry registry, StructurePoolBasedGenerator.PieceFactory pieceFactory, ChunkGenerator chunkGenerator, StructureManager structureManager, ChunkRandom chunkRandom, BlockBox blockBox, boolean bl, HeightLimitView heightLimitView, StructurePiecesCollector structurePiecesCollector, StructurePiecesGenerator.Context context, CallbackInfo ci, List list, int l, Box box, StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator) {
        onInit(structurePoolGenerator);
    }

    @Unique
    private static void onInit(StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator) {
        var config = Config.getInstance();
        int maxWaystoneCount = Utils.getRandomIntInRange(config.getMinPerVillage(), config.getMaxPerVillage());
        ((StructurePoolBasedGenerator_StructurePoolGeneratorAccess) (Object) structurePoolGenerator).setMaxWaystoneCount(maxWaystoneCount);
    }

}
