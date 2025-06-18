package wraith.fwaystones.client.models;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public interface BranchKeyGetter<K extends Record> {
    @Nullable
    K getKey(BlockRenderView blockView, BlockState state, BlockPos pos);
}
