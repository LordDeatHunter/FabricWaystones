package wraith.fwaystones.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import wraith.fwaystones.registry.ItemRegistry;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(WaystoneBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = new ItemStack(ItemRegistry.get("abyss_watcher"));

        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);
        matrices.translate(1f, 3.5f, 1f);

        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(entity.lookingRotR));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
        //matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(entity.lookingRotH));
        if (entity.getCachedState().get(WaystoneBlock.ACTIVE)) {
            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.FIXED, lightAbove, overlay, matrices, vertexConsumers, (int) entity.getPos().asLong());
        }

        matrices.pop();
    }

}
