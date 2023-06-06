package wraith.fwaystones.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.access.ClientPlayerEntityMixinAccess;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Inject(method = "handleRespawn", at = @At("RETURN"))
    public void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (this.minecraft.player != null) {
            ((ClientPlayerEntityMixinAccess) this.minecraft.player).requestSync();
        }
    }
    @Inject(method = "handleLogin", at = @At("RETURN"))
    public void onPlayerSpawn(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (this.minecraft.player != null) {
            ((ClientPlayerEntityMixinAccess) this.minecraft.player).requestSync();
        }
    }
}