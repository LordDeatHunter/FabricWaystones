package wraith.fwaystones.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

	@Inject(method = "restoreFrom", at = @At("HEAD"))
	public void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
		((PlayerEntityMixinAccess) this).learnWaystones(oldPlayer);
	}

}
