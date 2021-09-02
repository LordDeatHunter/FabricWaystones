package wraith.waystones.mixin;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wraith.waystones.util.Utils;

import java.util.concurrent.Executor;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    @ModifyVariable(method = "populateNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    public StructureAccessor populateNoise(StructureAccessor sa, Executor executor, StructureAccessor accessor, Chunk chunk) {
        return Utils.populateNoise(accessor, chunk);
    }

}
