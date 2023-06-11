package wraith.fwaystones.registry;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;

import java.util.function.BiConsumer;

public class BlockEntityRendererRegister {
	public static void register(BiConsumer<BlockEntityType<? extends BlockEntity>, BlockEntityRendererProvider> consumer) {
		consumer.accept(BlockEntityRegister.WAYSTONE_BLOCK_ENTITY.get(), WaystoneBlockEntityRenderer::new);
	}
}

