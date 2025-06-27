package wraith.fwaystones.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import wraith.fwaystones.registry.WaystoneItems;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(WaystoneBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.controllerStack();

        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);

        var heightOffset = 3.5f;

        if (((WaystoneBlock) entity.getCachedState().getBlock()).singleBlock()) {
            heightOffset = 1.625f;
        }

        matrices.translate(1f, heightOffset, 1f);

        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entity.lookingRotR));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        //matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(entity.lookingRotH));
        if (!stack.isEmpty()) {
            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(
                    stack, ModelTransformationMode.FIXED, lightAbove, overlay,
                    matrices, vertexConsumers, entity.getWorld(), (int) entity.getPos().asLong()
            );
        }

        matrices.pop();
    }
}
