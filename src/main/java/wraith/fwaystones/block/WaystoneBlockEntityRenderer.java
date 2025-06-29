package wraith.fwaystones.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(WaystoneBlockEntity waystone, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = waystone.controllerStack();

        if (!stack.isEmpty()) {
            matrices.push();
            matrices.translate(0.5, waystone.getControllerHeight(), 0.5f);
            var height = waystone.ticks + tickDelta;
            matrices.translate(0.0F, 0.1F + MathHelper.sin(height * 0.1F) * 0.01F, 0.0F);

            matrices.multiply(waystone.lastControllerRotation.nlerp(waystone.controllerRotation, tickDelta * 0.025f));
            matrices.translate(0, -0.125, 0);

//            matrices.multiply(waystone.controllerRotation);

            if (waystone.getWorld() != null) light = WorldRenderer.getLightmapCoordinates(waystone.getWorld(), waystone.getPos());
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, waystone.getWorld(), waystone.getPos().hashCode());

            matrices.pop();
        }

    }
}
