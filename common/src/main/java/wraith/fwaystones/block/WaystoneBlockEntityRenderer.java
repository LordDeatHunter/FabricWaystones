package wraith.fwaystones.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.registry.ItemRegister;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {
	private static final ItemStack stack = new ItemStack(ItemRegister.ABYSS_WATCHER.get());

	public WaystoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(WaystoneBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int overlay) {
		poseStack.pushPose();
		poseStack.scale(0.5f, 0.5f, 0.5f);
		poseStack.translate(1f, 3.5f, 1f);

		poseStack.mulPose(Axis.YN.rotationDegrees(blockEntity.lookingRotR));
		poseStack.mulPose(Axis.YP.rotationDegrees(90));
		//matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(blockEntity.lookingRotH));
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
