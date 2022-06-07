package wraith.fwaystones.mixin;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.FabricWaystones;

// TODO: Remove compat stuff
@Mixin(Identifier.class)
public class IdentifierMixin {

    @Mutable
    @Shadow @Final protected String namespace;

    @Inject(method = "<init>([Ljava/lang/String;)V", at = @At("TAIL"))
    private void changeNamespace(String[] id, CallbackInfo ci) {
        if (this.namespace.equals("waystones")) {
            this.namespace = FabricWaystones.MOD_ID;
        }
    }

}
