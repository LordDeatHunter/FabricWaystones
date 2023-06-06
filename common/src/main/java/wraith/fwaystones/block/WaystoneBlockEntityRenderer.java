package wraith.fwaystones.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.registry.ItemReg;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {
    public WaystoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(WaystoneBlockEntity blockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        ItemStack stack = new ItemStack(ItemReg.ABYSS_WATCHER.get());

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(1f, 3.5f, 1f);

        poseStack.mulPose(Vector3f.YN.rotationDegrees(blockEntity.lookingRotR));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
        //poseStack.mulPose(Vector3f.XN.rotationDegrees(entity.lookingRotH));
        if (blockEntity.getBlockState().getValue(WaystoneBlock.ACTIVE)) {
            int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    (LivingEntity)null,
                    stack,
                    ItemTransforms.TransformType.FIXED,
                    false,
                    poseStack,
                    multiBufferSource,
                    blockEntity.getLevel(),
                    lightAbove,
                    overlay,
                    (int) blockEntity.getBlockPos().asLong()
            );
        }
        poseStack.popPose();
    }
}