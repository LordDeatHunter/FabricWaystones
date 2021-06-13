package wraith.waystones.mixin;

import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wraith.waystones.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @ModifyVariable(method = "populateNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    public StructureAccessor populateNoise(StructureAccessor sa, Executor executor, StructureAccessor accessor, Chunk chunk) {
        if (!Config.getInstance().generateInVillages()) {
            return accessor;
        }
        ChunkPos chunkPos = chunk.getPos();
        for(int i = 0; i < StructureFeature.JIGSAW_STRUCTURES.size(); ++i) {
            StructureFeature<?> structureFeature = StructureFeature.JIGSAW_STRUCTURES.get(i);
            AtomicInteger waystones = new AtomicInteger(0);
            accessor.getStructuresWithChildren(ChunkSectionPos.from(chunkPos, 0), structureFeature).forEach((structures) -> {
                int pre = structures.getChildren().size();
                ArrayList<Integer> toRemove = new ArrayList<>();
                for (int j = 0; j < pre; ++j) {
                    StructurePiece structure = structures.getChildren().get(j);
                    if (structure instanceof PoolStructurePiece &&
                        ((PoolStructurePiece) structure).getPoolElement() instanceof SinglePoolElement &&
                        "waystones:village_waystone".equals(((SinglePoolElementAccessor)((PoolStructurePiece) structure).getPoolElement()).getLocation().left().get().toString()) &&
                        waystones.getAndIncrement() > 0) {
                        toRemove.add(j);
                    }
                }
                toRemove.sort(Collections.reverseOrder());
                for(int remove : toRemove) {
                    structures.getChildren().remove(remove);
                }
            });
        }
        return accessor;
    }

}
