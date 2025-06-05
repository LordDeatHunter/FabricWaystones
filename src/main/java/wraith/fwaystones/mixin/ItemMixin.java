package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.item.WaystoneComponentEventHooks;

@Mixin(Item.class)
public abstract class ItemMixin {
    @WrapMethod(method = "getTranslationKey(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;")
    private String fabricWaystone$adjustKey(ItemStack stack, Operation<String> original) {
        var alternativeKey = WaystoneComponentEventHooks.getTranslationKey(stack);

        if (alternativeKey != null) return alternativeKey;

        return original.call(stack);
    }
}
