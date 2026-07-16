package wraith.fwaystones.registry;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;

public final class CustomBlockEntityRendererRegistry {

    public static void RegisterBlockEntityRenderers() {
        BlockEntityRenderers.register(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
    }

}
