package wraith.waystones.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.waystones.screens.PocketWormholeScreenHandler;
import wraith.waystones.screens.WaystoneScreenHandler;

@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> {

    @Shadow @Final protected T handler;

    @Inject(method = "drawForeground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 1), cancellable = true)
    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        if (handler instanceof WaystoneScreenHandler || handler instanceof PocketWormholeScreenHandler) {
            ci.cancel();
        }
    }

}
