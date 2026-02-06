//package wraith.fwaystones.mixin;
//
//import net.minecraft.item.CompassItem;
//import net.minecraft.item.ItemUsageContext;
//import net.minecraft.util.ActionResult;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//import wraith.fwaystones.item.WaystoneComponentEventHooks;
//
//@Mixin(CompassItem.class)
//public abstract class CompassItemMixin {
//    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"), remap = false)
//    private void WaystoneLodeStone(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
//        var alternativeKey = WaystoneComponentEventHooks.getTranslationKey(stack);
//
//        if (alternativeKey != null) return alternativeKey;
//
//        return original.call(stack);
//    }
//}
