package wraith.fwaystones.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.access.ClientPlayerEntityMixinAccess;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            ((ClientPlayerEntityMixinAccess) MinecraftClient.getInstance().player).requestSync();
        }
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    public void onPlayerSpawn(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            ((ClientPlayerEntityMixinAccess) MinecraftClient.getInstance().player).requestSync();
        }
    }

}
