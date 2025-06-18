package wraith.fwaystones.item.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.registry.WaystoneItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaystoneCompassItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static ThreadLocal<@Nullable LivingEntity> livingHolder = ThreadLocal.withInitial(() -> null);

    private static final Map<UUID, WaystonePointer> waystonePointers = new HashMap<>();

    @Override
    public void render(
        ItemStack stack,
        ModelTransformationMode renderMode,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay
    ) {
        var client = MinecraftClient.getInstance();

        var world = client.world;
        if (world == null) return;

        var player = client.player;
        if (player == null) return;

        var storage = WaystoneDataStorage.getStorage(player);
        var playerStorage = WaystonePlayerData.getData(player);

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);

        var itemRenderer = client.getItemRenderer();
        itemRenderer.renderItem(
            stack,
            renderMode,
            renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND,
            matrices,
            vertexConsumers,
            light,
            overlay,
            itemRenderer.getModels().getModelManager().getModel(FabricWaystones.id("item/waystone_compass_base"))
        );

        Entity holder = stack.getHolder();
        if (holder == null) holder = livingHolder.get();
        if (holder == null || renderMode.isFirstPerson()) holder = client.getCameraEntity();
        if (holder == null && renderMode == ModelTransformationMode.GUI) holder = client.getCameraEntity();
        if (holder == null) {
            matrices.pop();
            return;
        }

//        if (renderMode != ModelTransformationMode.GUI || holder == client.getCameraEntity()) {
//            var rotation = holder.getRotationClient();
//            matrices.multiply(new Quaternionf().rotateXYZ(
//                (float) Math.toRadians(rotation.x),
//                (float) Math.toRadians(rotation.y + 180),
//                0
//            ));
//        }

//        matrices.multiply();

        for (UUID uuid : playerStorage.discoveredWaystones()) {
            var waystone = storage.getData(uuid);
            if (waystone == null) continue;
            var waystonePosisiton = storage.getPosition(waystone);
            if (waystonePosisiton == null) continue;
//            if (!player.getWorld().getRegistryKey().toString().equals(waystonePosisiton.worldName())) continue;

            var pointer = waystonePointers.computeIfAbsent(uuid, k -> new WaystonePointer());
            pointer.update(holder, waystonePosisiton.blockPos().up().toBottomCenterPos(), world.getTime());

            var direction = pointer.value;
            direction = direction.rotateY((float) Math.toRadians(holder.getBodyYaw() - 180));

//            pos = pos.rotateY((float) Math.toRadians((firstPerson ? entity.getHeadYaw() : entity.getBodyYaw())));
//            if (firstPerson) pos = pos.rotateX((float) Math.toRadians(entity.getPitch() - 180));

            var distance = pointer.distance;
            if (distance > 10000) continue;

            var distanceScale = Math.max(1, distance / 1000f);
            var scale = 1f / (float) distanceScale;

            matrices.push();

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.toDegrees(Math.atan2(direction.x, direction.z))));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) -Math.toDegrees(Math.asin(direction.y))));
            matrices.translate(0, 0, 1);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Math.toDegrees(Math.asin(direction.y))));

            matrices.scale(scale, scale, scale);


            matrices.scale(1/4f, 1/4f, 1/4f);
            client.getItemRenderer().renderItem(
                WaystoneItems.get("waystone").getDefaultStack(),
                ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                client.world,
                0
            );

            matrices.translate(0, 0.75f, 0);

            matrices.scale(1/64f, 1/64f, 1/64f);

            var rotation = player.getPos().subtract(holder.getPos()).normalize();
            matrices.translate(rotation.getX(), rotation.getY(), rotation.getZ());
            matrices.multiply(new Quaternionf().rotateXYZ(
                (float) Math.toRadians(rotation.x),
                (float) Math.toRadians(rotation.y + 180),
                0
            ));

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

            matrices.translate(client.textRenderer.getWidth(waystone.parsedName()) / -2f, 0, 0);

            client.textRenderer.draw(
                waystone.parsedName(),
                0,
                0,
                Colors.WHITE,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                light
            );

            matrices.pop();
        }
        matrices.pop();
    }

    @Environment(EnvType.CLIENT)
    private static class WaystonePointer {
        Vec3d value;
        Double distance;
        @Nullable private Long lastUpdated;

        public void update(Entity holder, Vec3d targetPos, long time) {
            if (!shouldUpdate(time)) return;
            var target = getDirection(holder, targetPos);
            var jump = lastUpdated == null || lastUpdated - time > 1000L;
            this.value = (value == null || jump) ? target : value.lerp(target, 0.1f);
            var d = holder.getPos().distanceTo(targetPos);
            this.distance = (distance == null || jump) ? d : MathHelper.lerp(0.1f, distance, d);
            this.lastUpdated = time;
        }

        private static Vec3d getDirection(Entity entity, Vec3d target) {
            return target.subtract(entity.getPos()).normalize();
        }


        private boolean shouldUpdate(long time) {
            return lastUpdated == null || lastUpdated != time;
        }

    }
}
