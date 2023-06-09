package wraith.fwaystones.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.registry.ItemRegistry;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {
	public WaystoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}
	@Override
	public void render(WaystoneBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int overlay) {
		ItemStack stack = new ItemStack(ItemRegistry.ABYSS_WATCHER.get());

		poseStack.pushPose();
		poseStack.scale(0.5f, 0.5f, 0.5f);
		poseStack.translate(1f, 3.5f, 1f);

		poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.lookingRotR));
		poseStack.mulPose(Axis.YP.rotationDegrees(90));
		//matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(entity.lookingRotH));
		if (blockEntity.getBlockState().getValue(WaystoneBlock.ACTIVE)) {
			int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
			Minecraft.getInstance().getItemRenderer().renderStatic(
					stack,
					ItemDisplayContext.FIXED,
					lightAbove,
					overlay,
					poseStack,
					multiBufferSource,
					blockEntity.getLevel(),
					(int) blockEntity.getBlockPos().asLong()
			);
		}

		poseStack.popPose();
	}
}
