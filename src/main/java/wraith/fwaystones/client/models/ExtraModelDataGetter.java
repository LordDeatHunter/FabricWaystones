package wraith.fwaystones.client.models;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public interface ExtraModelDataGetter<E> {
    E getData(BlockRenderView blockView, BlockPos pos, BlockState state);
}
