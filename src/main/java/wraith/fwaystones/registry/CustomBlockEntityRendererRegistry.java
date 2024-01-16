package wraith.fwaystones.registry;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;

public final class CustomBlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers() {
        BlockEntityRendererFactories.register(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
    }

}
