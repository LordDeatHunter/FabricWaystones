package wraith.fwaystones.mixin.xaeros;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.misc.Misc;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;

@Mixin(xaero.map.mods.gui.WaypointRenderer.class)
public abstract class WaypointRendererMixin {

    @WrapOperation(
        method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
        at = @At(value = "INVOKE", target = "Lxaero/map/mods/gui/Waypoint;getColor()I", remap = false))
    private int modifyFullscreenWaypointColor(
        xaero.map.mods.gui.Waypoint instance,
        Operation<Integer> original,
        @Share(namespace = FabricWaystones.MOD_ID, value = "WaystoneData") LocalRef<@Nullable NetworkedWaystoneData> sharedData
    ) {
        if (instance.getOriginal() instanceof Waypoint w) {
            var data = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneData(w);

            if (data instanceof NetworkedWaystoneData networkedData) {
                sharedData.set(networkedData);
                return data.color();
            }
        }

        return original.call(instance);
    }

    @WrapOperation(
        method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
        at = @At(value = "INVOKE", target = "Lxaero/map/graphics/MapRenderHelper;blitIntoMultiTextureRenderer(Lorg/joml/Matrix4f;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRenderer;FFIIIIFFFFIII)V", remap = false))
    private void modifyFullscreenWaypointIcon(
        Matrix4f matrix,
        MultiTextureRenderTypeRenderer renderer,
        float x, float y,
        int u, int v,
        int width, int height,
        float r, float g, float b, float a,
        int textureWidth, int textureHeight,
        int texture,
        Operation<Void> original,
        @Local(argsOnly = true) DrawContext ctx,
        @Local MatrixStack matrices,
        @Share(namespace = FabricWaystones.MOD_ID, value = "WaystoneData") LocalRef<@Nullable NetworkedWaystoneData> sharedData
    ) {
        var data = sharedData.get();
        if (data != null) {
            matrices.push();

//            matrices.scale(0.65f, 0.65f, 0.65f);

            ctx.drawTexture(
                data.type().getIconLocation(),
                -9, -16, 0, 0, 16, 16, 16, 16
            );

            matrices.pop();
        } else {
            original.call(matrix, renderer, x, y, u, v, width, height, r, g, b, a, textureWidth, textureHeight, texture);
        }
    }

    @WrapOperation(
        method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", remap = false))
    private int modifyFullscreenWaypointNameWidth(
            TextRenderer instance,
            String text,
            Operation<Integer> original, @Share(namespace = FabricWaystones.MOD_ID, value = "WaystoneData") LocalRef<@Nullable NetworkedWaystoneData> sharedData
    ) {
        var data = sharedData.get();

        return (data != null) ? instance.getWidth(data.parsedName()) : original.call(instance, text);
    }

    @WrapOperation(
        method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
        at = @At(value = "INVOKE", target = "Lxaero/map/misc/Misc;drawNormalText(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFIZLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V", remap = false))
    private void modifyFullscreenWaypointDisplayText(
        MatrixStack matrices,
        String name,
        float x, float y,
        int color,
        boolean shadow,
        VertexConsumerProvider.Immediate renderTypeBuffer,
        Operation<Void> original,
        @Share(namespace = FabricWaystones.MOD_ID, value = "WaystoneData") LocalRef<@Nullable NetworkedWaystoneData> sharedData
    ) {
        var data = sharedData.get();
        if (data == null) {
            original.call(matrices, name, x, y, color, shadow, renderTypeBuffer);
        } else {
            Misc.drawNormalText(matrices, data.parsedName(), x, y, color, shadow, renderTypeBuffer);
        }
    }
}
