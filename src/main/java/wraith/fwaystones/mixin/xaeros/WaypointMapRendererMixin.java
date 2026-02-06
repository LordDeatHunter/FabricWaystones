package wraith.fwaystones.mixin.xaeros;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import xaero.common.minimap.waypoints.Waypoint;

@Mixin(xaero.hud.minimap.waypoint.render.WaypointMapRenderer.class)
public abstract class WaypointMapRendererMixin {


    @ModifyExpressionValue(method = "drawIconOnGUI(Lnet/minecraft/client/gui/DrawContext;Lxaero/common/minimap/render/MinimapRendererHelper;Lxaero/common/minimap/waypoints/Waypoint;IIILnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/VertexConsumer;)V", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/waypoint/WaypointColor;getHex()I", remap = false))
    private int testIfWaypointIsFromWaystone(int original, @Local(argsOnly = true) Waypoint w) {
        var data = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneData(w);
        return (data != null) ? data.color() : original;
    }
}
