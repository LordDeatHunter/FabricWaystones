package wraith.waystones.registries;

import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import wraith.waystones.block.WaystoneBlockEntityRenderer;

public final class CustomBlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers(){
        BlockEntityRendererRegistry.INSTANCE.register(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
    }

}
