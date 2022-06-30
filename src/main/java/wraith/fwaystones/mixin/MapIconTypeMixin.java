package wraith.fwaystones.mixin;

import net.minecraft.item.map.MapIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.item.map.MapIconTypeAccessor;

@Mixin(MapIcon.Type.class)
public class MapIconTypeMixin implements MapIconTypeAccessor {
    public boolean isWaystone = false;

    @Override
    public MapIcon.Type isWaystone(boolean bl) {
        isWaystone = bl;
        return (MapIcon.Type)(Object)this;
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    private void checkIfWaystone(CallbackInfoReturnable<Byte> cir) {
        if (isWaystone)
            cir.setReturnValue((byte) 27);
    }
}
