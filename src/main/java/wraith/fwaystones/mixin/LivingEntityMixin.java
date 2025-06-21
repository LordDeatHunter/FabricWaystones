package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.item.WaystoneComponentEventHooks;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(method = "tryUseTotem")
    public boolean revive(DamageSource source, Operation<Boolean> original) {
        if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            var stack = WaystoneComponentEventHooks.getVoidTotem(player);

            if (stack != null && WaystoneComponentEventHooks.attemptVoidTotemEffects(player, stack, source)) {
                return true;
            }
        }

        return original.call(source);
    }

}
