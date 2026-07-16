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
import net.minecraft.world.phys.Vec3;
import wraith.fwaystones.registry.ItemRegistry;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(WaystoneBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Vec3 cameraPos) {
        ItemStack stack = new ItemStack(ItemRegistry.get("abyss_watcher"));

        matrices.pushPose();
        matrices.scale(0.5f, 0.5f, 0.5f);
        matrices.translate(1f, 3.5f, 1f);

        matrices.mulPose(Axis.YN.rotationDegrees(entity.lookingRotR));
        matrices.mulPose(Axis.YP.rotationDegrees(90));
        //matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(entity.lookingRotH));
        if (entity.getBlockState().getValue(WaystoneBlock.ACTIVE)) {
            int lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.FIXED, lightAbove, overlay,
                    matrices, vertexConsumers, entity.getLevel(), (int) entity.getBlockPos().asLong()
            );
        }

        matrices.popPose();
    }

}
