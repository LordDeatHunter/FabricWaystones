package wraith.fwaystones;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.registry.BlockEntityRegistry;

import java.util.function.BiConsumer;

public final class WaystonesClient {
    public static void registerBlockEntityRenderers(BiConsumer<BlockEntityType<? extends BlockEntity>, BlockEntityRendererProvider> consumer){
        consumer.accept(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY.get(), WaystoneBlockEntityRenderer::new);
    }
}
