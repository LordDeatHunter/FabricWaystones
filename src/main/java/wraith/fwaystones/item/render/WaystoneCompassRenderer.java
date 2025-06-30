package wraith.fwaystones.item.render;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.Named;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.item.components.WaystoneDataHolder;
import wraith.fwaystones.item.components.WaystoneTyped;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.registry.WaystoneItems;

import java.time.Duration;
import java.util.*;

public class WaystoneCompassRenderer {
    public static final Multimap<UUID, ItemStack> WAYSTONE_COMPASS_STACKS = HashMultimap.create();

    private static final LoadingCache<UUID, Map<UUID, WaystonePointer>> waystonePointers = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .expireAfterAccess(Duration.ofSeconds(30))
        .build(CacheLoader.from(() -> new HashMap<>()));

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            var client = MinecraftClient.getInstance();

            var player = client.player;
            if (player == null) return;

            var world = client.world;
            if (world == null) return;

            var matrices = context.matrixStack();
            if (matrices == null) return;

            for (Hand hand : Hand.values()) {
                var stack = player.getStackInHand(hand);
                if (stack.getItem() != WaystoneItems.WAYSTONE_COMPASS) continue;
                WaystoneCompassRenderer.WAYSTONE_COMPASS_STACKS.put(player.getUuid(), stack);
            }

            var tickDelta = client.getRenderTickCounter().getTickDelta(true);

            matrices.push();
            var pos = context.camera().getPos();
            matrices.translate(-pos.x, -pos.y, -pos.z);
            pos = player.getLerpedPos(tickDelta);
            matrices.translate(pos.x, pos.y, pos.z);
            WaystoneCompassRenderer.render(
                player,
                matrices,
                context.consumers(),
                client.getEntityRenderDispatcher().getLight(player, tickDelta),
                OverlayTexture.DEFAULT_UV,
                tickDelta
            );
            matrices.pop();
        });

//        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            waystonePointers.asMap().forEach((uuid, pointers) -> {
//                if (!activePointers.contains(uuid)) {
//                    waystonePointers.invalidate(uuid);
//                } else {
//                    pointers.values().removeIf(pointer -> pointer.value == null || pointer.value.equals(Vec3d.ZERO));
//                }
//            });
//            activePointers.clear();
//        });
    }

    public static void render(
        Entity holder,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        float tickDelta
    ) {
        var client = MinecraftClient.getInstance();

        var world = client.world;
        if (world == null) return;

        var player = client.player;
        if (player == null) return;

//        var storage = WaystoneDataStorage.getStorage(player);
        var owner = getOwner(holder);
        var playerStorage = WaystonePlayerData.getData(owner != null ? owner : player);

        matrices.push();

        matrices.translate(0, holder.getEyeHeight(holder.getPose()), 0);

        for (ItemStack compass : WAYSTONE_COMPASS_STACKS.removeAll(holder.getUuid())) {
            drawWaystonePointers(
                compass,
                matrices,
                vertexConsumers,
                playerStorage,
                holder.getUuid(),
                player.getWorld(),
                holder.getLerpedPos(tickDelta).add(0, holder.getEyeHeight(holder.getPose()), 0),
                light,
                overlay,
                tickDelta,
                1
            );
        }
        matrices.pop();
    }

    public static void drawWaystonePointers(
        ItemStack compass,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        @Nullable WaystonePlayerData waystonePlayerData,
        UUID holderUUID,
        World world,
        Vec3d holderPos,
        int light,
        int overlay,
        float tickDelta,
        double scale
    ) {
        var client = MinecraftClient.getInstance();
        var storage = WaystoneDataStorage.getStorage(world);

        var pointers = waystonePointers.getUnchecked(holderUUID);

        var waystones = (waystonePlayerData != null ? waystonePlayerData.discoveredWaystones().stream() : storage.getAllIds().stream())
            .map(storage::getData)
            .filter(Objects::nonNull)
            .map(waystoneData -> new Pair<>(waystoneData, storage.getPosition(waystoneData)))
            .filter(pair -> pair.getRight() != null)
            .sorted(Comparator.comparingDouble(pair -> pair.getRight().blockPos().up().toBottomCenterPos().distanceTo(holderPos)))
            .toList();

        matrices.push();
        for (Pair<WaystoneData, WaystonePosition> pair : waystones) {
            var waystone = pair.getLeft();
            var waystonePosition = pair.getRight();
            if (!world.getRegistryKey().equals(waystonePosition.worldKey())) continue;

            var waystonePos = waystonePosition.blockPos().up().toBottomCenterPos();

            var pointer = pointers.computeIfAbsent(waystone.uuid(), k -> new WaystonePointer());
            pointer.update(holderPos, waystonePos, waystones.size() - waystones.indexOf(pair));

            if (Math.abs(1 - pointer.value) < 0.1f) continue;
            var displayPos = Vec3d.ZERO.lerp(waystonePos.subtract(holderPos), pointer.value);

            matrices.push();
            matrices.translate(displayPos.x, displayPos.y, displayPos.z);

            displayPos.multiply(scale);

            var stack = WaystoneItems.WAYSTONE.getDefaultStack();
            stack.set(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(waystone.type().getId()));
            stack.set(WaystoneDataComponents.DATA_HOLDER, new WaystoneDataHolder(waystone));

            matrices.scale(1 / 6f, 1 / 6f, 1 / 6f);

            matrices.push();
            var iconDirection = displayPos.normalize().multiply(scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.atan2(iconDirection.x, iconDirection.z)));
            client.getItemRenderer().renderItem(
                stack,
                ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                client.world,
                0
            );
            matrices.pop();

            matrices.translate(0, 0.75f, 0);

            matrices.scale(1 / 64f, 1 / 64f, 1 / 64f);

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));

            var camDirection = displayPos.add(holderPos).subtract(client.gameRenderer.getCamera().getPos()).normalize();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.atan2(-camDirection.x, camDirection.z)));

            drawLabel(client, Text.literal(String.format("%.1f", holderPos.distanceTo(waystonePos))), matrices, vertexConsumers, light);
            if (waystone instanceof Named named) drawLabel(client, named.parsedName(), matrices, vertexConsumers, light);

            matrices.pop();
        }
        matrices.pop();
    }

    private static void drawLabel(
        MinecraftClient client,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        matrices.push();
        matrices.translate(client.textRenderer.getWidth(text) / -2f, 0, 0);
        client.textRenderer.draw(
            text,
            0, 0,
            Colors.WHITE,
            false,
            matrices.peek().getPositionMatrix(),
            vertexConsumers,
            TextRenderer.TextLayerType.NORMAL,
            (int) (client.options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24,
            light
        );
        matrices.pop();
        matrices.translate(0, -(client.textRenderer.fontHeight + 1), 0);
    }

    private static @Nullable PlayerEntity getOwner(Entity entity) {
        if (entity instanceof PlayerEntity player) return player;
        if (entity instanceof Tameable tameable && tameable.getOwner() instanceof PlayerEntity player) return player;
        if (entity instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity player) return player;
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static class WaystonePointer {
        private static final long MS_TO_NS = 1000000L;

        double value = 0;
        boolean lerping = true;

        private long lastInvocationTime = 0;

        public void update(Vec3d holderPos, Vec3d targetPos, int index) {
            var lastUpdated = lastInvocationTime;
            var delta = getFrameDelta() * 0.25f;
            var timeSinceLastUpdate = (lastInvocationTime - lastUpdated) / MS_TO_NS;

            var distance = holderPos.distanceTo(targetPos);
            var farEnough = distance > 8f;
            var target = farEnough ? 1f / distance : 1f;

            if (timeSinceLastUpdate > 1000L) {
                lerping = false;
            } else if (!farEnough) {
                lerping = true;
            } else {
                lerping = Math.abs(value - target) > 0.0025f;
            }

            value = lerping ? MathHelper.lerp(delta, value, target) : target;

//            targetPos = targetPos.subtract(holderPos);
//            if (!stick) targetPos = targetPos.normalize();
//
//            if ((value.distanceTo(targetPos) < 1 && stick) || timeSinceLastUpdate > 1000) {
//                value = targetPos;
//            } else {
//                value = value.lerp(targetPos, delta * 0.25f);
//            }

//            var jump = lastUpdated - time > 1000L;
//            this.value = (value == null || jump) ? target : value.lerp(target, delta);
//            var d = holder.getPos().distanceTo(targetPos);
//            this.distance = (distance == null || jump) ? d : MathHelper.lerp(delta, distance, d);
//            this.lastUpdated = time;
        }

        public float getFrameDelta() {
            long measuringTime = Util.getMeasuringTimeNano();
            float frameDelta = 0f;

            if (!MinecraftClient.getInstance().isPaused()) frameDelta = (float) ((double) (measuringTime - lastInvocationTime) / MS_TO_NS / 50d);

            lastInvocationTime = measuringTime;
            return frameDelta;
        }

    }
}
