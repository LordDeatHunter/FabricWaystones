package wraith.waystones.registries;

import wraith.waystones.block.WaystoneBlockEntityRenderer;

public final class BlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers(){
        net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry.INSTANCE.register(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
    }

}
