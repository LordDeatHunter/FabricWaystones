package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;

public final class CustomBlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers() {
        BlockEntityRendererRegistry.register(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
    }

}
