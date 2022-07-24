package wraith.fwaystones.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.map.MapIcon;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import wraith.fwaystones.item.map.MapIconAccessor;

@Mixin(targets = "net.minecraft.client.render.MapRenderer$MapTexture")
public class MapTextureMixin {

    @Unique
    private static final RenderLayer fwaystones$WAYSTONE_ICON_RENDER_LAYER = RenderLayer.getText(new Identifier("fwaystones", "textures/map/waystone_icon.png"));

    @Unique
    private boolean fwaystones$is_waystone_icon_cache;

    @ModifyVariable(method = "draw", at = @At(value = "LOAD", ordinal = 4), ordinal = 0)
    private MapIcon check_if_waystone_icon(MapIcon icon) {
        fwaystones$is_waystone_icon_cache = ((MapIconAccessor) icon).getIsWaystone();
        return icon;
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 1)
    private float modify_g(float x) {
        return fwaystones$is_waystone_icon_cache ? 0.0f : x;
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 2)
    private float modify_h(float x) {
        return fwaystones$is_waystone_icon_cache ? 0.0f : x;
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 3)
    private float modify_l(float x) {
        return fwaystones$is_waystone_icon_cache ? 1.0f : x;
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 4)
    private float modify_m(float x) {
        return fwaystones$is_waystone_icon_cache ? 1.0f : x;
    }

    @ModifyArg(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;", ordinal = 1))
    private RenderLayer waystoneRenderLayer(RenderLayer rl) {
        if (fwaystones$is_waystone_icon_cache) {
            fwaystones$is_waystone_icon_cache = false;
            return fwaystones$WAYSTONE_ICON_RENDER_LAYER;
        } else return rl;
    }
}
