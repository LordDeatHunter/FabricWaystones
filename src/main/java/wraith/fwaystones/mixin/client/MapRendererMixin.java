package wraith.fwaystones.mixin.client;

import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.Named;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.pond.MapStateDuck;

@Environment(EnvType.CLIENT)
@Mixin(MapRenderer.MapTexture.class)
public abstract class MapRendererMixin {

    @Shadow private MapState state;

    @Inject(method = "draw", at = @At("RETURN"))
    private void drawWaystoneMarkers(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        boolean hidePlayerIcons,
        int light,
        CallbackInfo ci
    ) {
        var client = MinecraftClient.getInstance();
        if (client == null) return;

        var player = client.player;
        if (player == null) return;

        var storage = WaystoneDataStorage.getStorage(client);
        if (storage == null) return;

        var playerStorage = WaystonePlayerData.getData(player);

        var markers = ((MapStateDuck) state).fwaystones$getWaystoneMarkers();
        if (markers.isEmpty()) return;

        var discovered = playerStorage.discoveredWaystones();

        var index = 0.5f;
        for (var marker : markers) {
            if (!discovered.contains(marker)) continue;

            var waystonePosition = storage.getPosition(marker);
            if (waystonePosition == null) continue;
            var pos = waystonePosition.blockPos();

            var waystone = storage.getData(marker);
            if (waystone == null) continue;

            matrices.push();
            var scale = 1 << state.scale;
            var x = (pos.getX() + 64f) % (scale * 128f);
            var z = (pos.getZ() + 64f) % (scale * 128f);
            matrices.translate(x, z, -0.02F);
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            matrices.scale(4.0F, 4.0F, 3.0F);
            matrices.translate(-0.125F, 0.125F, 0.0F);

            var matrix = matrices.peek().getPositionMatrix();

            var waystoneSprite = ((SpriteAtlasHolderAccessor) client.getMapDecorationsAtlasManager()).fwaystones$getSprite(WaystoneTypes.getIdOrDefault(waystone.type()).withPath(s -> s + "_waystone"));
            var waystoneBuffer = vertexConsumers.getBuffer(RenderLayer.getText(waystoneSprite.getAtlasId()));
            waystoneBuffer.vertex(matrix, -1.0F, 1.0F, index * -0.001F).color(Colors.WHITE).texture(waystoneSprite.getMinU(), waystoneSprite.getMinV()).light(light);
            waystoneBuffer.vertex(matrix, 1.0F, 1.0F, index * -0.001F).color(Colors.WHITE).texture(waystoneSprite.getMaxU(), waystoneSprite.getMinV()).light(light);
            waystoneBuffer.vertex(matrix, 1.0F, -1.0F, index * -0.001F).color(Colors.WHITE).texture(waystoneSprite.getMaxU(), waystoneSprite.getMaxV()).light(light);
            waystoneBuffer.vertex(matrix, -1.0F, -1.0F, index * -0.001F).color(Colors.WHITE).texture(waystoneSprite.getMinU(), waystoneSprite.getMaxV()).light(light);

            var runeSprite = ((SpriteAtlasHolderAccessor) client.getMapDecorationsAtlasManager()).fwaystones$getSprite(FabricWaystones.id("waystone_runes"));
            var runeBuffer = vertexConsumers.getBuffer(RenderLayer.getText(runeSprite.getAtlasId()));
            var color = Color.ofRgb(waystone.color()).argb();
            runeBuffer.vertex(matrix, -1.0F, 1.0F, index * -0.001F).color(color).texture(runeSprite.getMinU(), runeSprite.getMinV()).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            runeBuffer.vertex(matrix, 1.0F, 1.0F, index * -0.001F).color(color).texture(runeSprite.getMaxU(), runeSprite.getMinV()).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            runeBuffer.vertex(matrix, 1.0F, -1.0F, index * -0.001F).color(color).texture(runeSprite.getMaxU(), runeSprite.getMaxV()).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            runeBuffer.vertex(matrix, -1.0F, -1.0F, index * -0.001F).color(color).texture(runeSprite.getMinU(), runeSprite.getMaxV()).light(LightmapTextureManager.MAX_LIGHT_COORDINATE);

            matrices.pop();
            var textRenderer = client.textRenderer;
            var name = waystone instanceof Named named ? named.parsedName() : Text.empty();
            float o = textRenderer.getWidth(name);
            float p = MathHelper.clamp(25.0F / o, 0.0F, 6.0F / 9.0F);
            matrices.push();
            matrices.translate(-o * p / 2.0F, 4.0F, -0.005F);
            matrices.scale(p, p, 1.0F);
            matrices.translate(0.0F, 0.0F, -0.1F);
            textRenderer.draw(name, 0.0F, 0.0F, Colors.WHITE, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, Integer.MIN_VALUE, light);
            matrices.pop();
            matrices.pop();
            index++;
        }

    }
}
