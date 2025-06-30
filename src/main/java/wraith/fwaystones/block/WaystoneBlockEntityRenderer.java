package wraith.fwaystones.block;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.item.render.WaystoneCompassRenderer;
import wraith.fwaystones.registry.WaystoneItems;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(WaystoneBlockEntity waystone, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = waystone.controllerStack();
        var world = waystone.getWorld();

        if (world != null) light = WorldRenderer.getLightmapCoordinates(waystone.getWorld(), waystone.getPos());


        if (!stack.isEmpty()) {
            matrices.push();
            matrices.translate(0.5, waystone.getControllerHeight(), 0.5f);

            //TODO: convention tag?
            if (stack.isOf(Items.CLOCK)) renderSky(waystone, MinecraftClient.getInstance(), matrices, vertexConsumers, waystone.getWorld(), tickDelta);
            if (stack.isOf(WaystoneItems.WAYSTONE_COMPASS)) renderWaystoneCompass(stack, waystone, tickDelta, matrices, vertexConsumers, light, overlay);

            matrices.translate(0.0F, 0.1F + MathHelper.sin((waystone.ticks + tickDelta) * 0.1F) * 0.01F, 0.0F);

            if (waystone.lastControllerRotation == null) waystone.lastControllerRotation = waystone.controllerRotation;
            if (Float.isNaN(waystone.lastControllerRotation.x)) waystone.lastControllerRotation.x = waystone.controllerRotation.x;
            if (Float.isNaN(waystone.lastControllerRotation.y)) waystone.lastControllerRotation.y = waystone.controllerRotation.y;
            if (Float.isNaN(waystone.lastControllerRotation.z)) waystone.lastControllerRotation.z = waystone.controllerRotation.z;
            if (Float.isNaN(waystone.lastControllerRotation.w)) waystone.lastControllerRotation.w = waystone.controllerRotation.w;
            waystone.lastControllerRotation.slerp(waystone.controllerRotation, tickDelta * 0.125f);

            if (stack.isIn(FabricWaystones.WAYSTONE_DISPLAY_ALIVE)) {
                matrices.multiply(waystone.lastControllerRotation);
            } else if (stack.isIn(FabricWaystones.WAYSTONE_DISPLAY_GYRO)) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((waystone.ticks + tickDelta) * 5f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((waystone.ticks + tickDelta) * 7.5f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((waystone.ticks + tickDelta) * 10f));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((waystone.ticks + tickDelta) * 1));
            }


//            EntityRenderDispatcher.drawVector(matrices, vertexConsumers.getBuffer(RenderLayer.LINES), Vec3d.ZERO.toVector3f(), new Vec3d(0, 0, -100), -16776961);

            //TODO: shoot laser at any nearby item entities that are about to despawn so it looks like the waystone did it

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            matrices.translate(0, -0.125, 0);

            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, waystone.getWorld(), waystone.getPos().hashCode());

            matrices.pop();
        }

    }

    private void renderSky(WaystoneBlockEntity waystone, MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, float tickDelta) {
        matrices.push();

        var scale = 1/32f;
        matrices.scale(scale, scale, scale);

        var culling = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        RenderSystem.disableCull();

        client.worldRenderer.renderSky(
            matrices.peek().getPositionMatrix(),
            matrices.peek().getPositionMatrix(),
            tickDelta,
            client.gameRenderer.getCamera(),
            false,
            () -> {}
        );

        if (culling) RenderSystem.enableCull();

        matrices.pop();
    }

    private void renderWaystoneCompass(ItemStack stack, WaystoneBlockEntity waystone, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        var scale = 4;
        matrices.scale(scale, scale, scale);

        WaystoneCompassRenderer.drawWaystonePointers(
            stack,
            matrices,
            vertexConsumers,
            MinecraftClient.getInstance().player != null ? WaystonePlayerData.getData(MinecraftClient.getInstance().player) : null,
            waystone.getUUID(),
            waystone.getWorld(),
            waystone.getControllerPos(),
            light,
            overlay,
            tickDelta,
            scale
        );

        matrices.pop();
    }
}
