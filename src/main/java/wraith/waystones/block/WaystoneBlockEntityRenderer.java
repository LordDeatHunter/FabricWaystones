package wraith.waystones.block;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import wraith.waystones.registries.ItemRegistry;

public class WaystoneBlockEntityRenderer extends BlockEntityRenderer<WaystoneBlockEntity> {

    public WaystoneBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(WaystoneBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getCachedState().get(WaystoneBlock.HALF) == DoubleBlockHalf.LOWER) {
            return;
        }
        ItemStack stack = new ItemStack(ItemRegistry.ITEMS.get("abyss_watcher"));

        matrices.push();
        matrices.scale(0.5f, 0.5f, 0.5f);
        matrices.translate(1f, 1.5f, 1f);
        //todo: make eye follow player
        PlayerEntity closestPlayer = entity.getWorld().getClosestPlayer(entity.getPos().getX() + 0.5, entity.getPos().getY() + 0.5, entity.getPos().getZ() + 0.5, 4.5, false);
        if(closestPlayer != null)
        {
            double x = closestPlayer.getX() - entity.getPos().getX() - 0.5;
            double z = closestPlayer.getZ() - entity.getPos().getZ() - 0.5;
            float rot = (float)MathHelper.atan2(z, x);
            matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-rot));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(80));
        }
        else {
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(entity.getWorld().getTime() + tickDelta));
        }

        
        int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.FIXED, lightAbove, overlay, matrices, vertexConsumers);
        matrices.pop();
    }

}
