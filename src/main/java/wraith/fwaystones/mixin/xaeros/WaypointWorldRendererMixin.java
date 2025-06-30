package wraith.fwaystones.mixin.xaeros;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import wraith.fwaystones.mixin.client.DrawContextAccessor;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.misc.Misc;

@Mixin(xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer.class)
public abstract class WaypointWorldRendererMixin {

    @Unique
    private static final String NAME_KEY = "fwaystones:will_be_replaced";

    @Unique
    private final ThreadLocal<Waypoint> WAYPOINT_STORAGE = ThreadLocal.withInitial(() -> null);

    @Inject(method = "renderIconWithLabels", at = @At("HEAD"))
    private void testIfWaypointIsFromWaystone(
        Waypoint w,
        boolean highlit,
        String name,
        String distanceText,
        String subWorldName,
        float iconScale,
        int nameScale,
        int distanceTextScale,
        TextRenderer fontRenderer,
        int halfIconPixel,
        MatrixStack matrixStack,
        VertexConsumerProvider.Immediate bufferSource,
        CallbackInfo ci,
        @Local(argsOnly = true, ordinal = 0) LocalRef<String> nameRef
    ) {
        var uuid = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneUUID(w);

        if (uuid != null) {
            nameRef.set("fwaystones:will_be_replaced");
        }
    }

    @WrapOperation(method = "renderIconWithLabels", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/waypoint/render/world/WaypointWorldRenderer;renderWaypointLabel(Ljava/lang/String;Lnet/minecraft/client/util/math/MatrixStack;Lxaero/common/minimap/render/MinimapRendererHelper;Lnet/minecraft/client/font/TextRenderer;IF)V", ordinal = 1))
    private void setupWaystoneUUIDCache(
        xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer instance,
        String label,
        MatrixStack matrixStack,
        MinimapRendererHelper helper,
        TextRenderer fontRenderer,
        int labelScale,
        float bgAlpha,
        Operation<Void> original,
        @Local(argsOnly = true) Waypoint w
    ) {
        var hasSetupUUID = label.equals(NAME_KEY);

        if (hasSetupUUID) WAYPOINT_STORAGE.set(w);

        original.call(instance, label, matrixStack, helper, fontRenderer, labelScale, bgAlpha);

        if(hasSetupUUID) WAYPOINT_STORAGE.remove();
    }

    @WrapOperation(method = "renderWaypointLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I"))
    private int getBetterWidth(
        TextRenderer instance,
        String text,
        Operation<Integer> original,
        @Share(value = "waystone_data", namespace = "fwaystones") LocalRef<NetworkedWaystoneData> waystoneDataRef
    ) {
        var waypoint = WAYPOINT_STORAGE.get();

        if (waypoint != null) {
            var data = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneData(waypoint);

            if (data instanceof NetworkedWaystoneData networkedData) {
                waystoneDataRef.set(networkedData);

                return instance.getWidth(networkedData.parsedName());
            }
        }

        return original.call(instance, text);
    }

    @WrapOperation(method = "renderWaypointLabel", at = @At(value = "INVOKE", target = "Lxaero/common/misc/Misc;drawNormalText(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFIZLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V"))
    private void drawCorrectText(MatrixStack matrices, String name, float x, float y, int color, boolean shadow, VertexConsumerProvider.Immediate renderTypeBuffer, Operation<Void> original, @Share(value = "waystone_data", namespace = "fwaystones") LocalRef<NetworkedWaystoneData> waystoneDataRef) {
        var data = waystoneDataRef.get();

        if (data != null) {
            Misc.drawNormalText(matrices, data.parsedName(), x, y, color, shadow, renderTypeBuffer);
        } else {
            original.call(matrices, name, x, y, color, shadow, renderTypeBuffer);
        }
    }

    @WrapMethod(method = "renderIcon")
    private void drawWaystoneIconInstead(Waypoint w, boolean highlit, MatrixStack matrixStack, TextRenderer fontRenderer, VertexConsumerProvider.Immediate bufferSource, Operation<Void> original) {
        var data = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneData(w);

        if (data != null) {
            var ctx = DrawContextAccessor.createContext(MinecraftClient.getInstance(), matrixStack, bufferSource);

            matrixStack.push();

            matrixStack.scale(0.65f, 0.65f, 0.65f);

            ctx.drawTexture(
                    data.type().getIconLocation(),
                    -9, -16, 0, 0, 16, 16, 16, 16
            );

            matrixStack.pop();
        } else {
            original.call(w, highlit, matrixStack, fontRenderer, bufferSource);
        }
    }

    @ModifyExpressionValue(method = "renderIcon", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/waypoint/WaypointColor;getHex()I", remap = false))
    private int testIfWaypointIsFromWaystone(int original, @Local(argsOnly = true) Waypoint w) {
        var data = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneData(w);

        return (data != null) ? data.color() : original;
    }
}
