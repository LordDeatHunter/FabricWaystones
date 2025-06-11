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
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import wraith.fwaystones.mixin.client.DrawContextAccessor;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.misc.Misc;

@Mixin(xaero.hud.minimap.waypoint.render.WaypointMapRenderer.class)
public abstract class WaypointMapRendererMixin {


    @ModifyExpressionValue(method = "drawIconOnGUI(Lnet/minecraft/client/gui/DrawContext;Lxaero/common/minimap/render/MinimapRendererHelper;Lxaero/common/minimap/waypoints/Waypoint;IIILnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/VertexConsumer;)V", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/waypoint/WaypointColor;getHex()I", remap = false))
    private int testIfWaypointIsFromWaystone(
        int original,
        @Local(argsOnly = true) Waypoint w
    ) {
        var uuid = XaerosMinimapWaypointMaker.INSTANCE.getWaystoneUUID(w);
        if (uuid == null) return original;
        var storage = WaystoneDataStorage.getStorage(MinecraftClient.getInstance());
        if (storage == null) return original;
        var data = storage.getData(uuid);
        if (data == null) return original;
        return data.color();
    }
}
