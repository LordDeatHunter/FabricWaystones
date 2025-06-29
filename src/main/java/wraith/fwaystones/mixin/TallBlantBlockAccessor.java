package wraith.fwaystones.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TallPlantBlock.class)
public interface TallBlantBlockAccessor {
    @Invoker("onBreakInCreative")
    static void fWaystones$onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        throw new IllegalArgumentException("Invoker should not be called directly!");
    }
}
