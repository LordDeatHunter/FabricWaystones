package wraith.fwaystones.mixin;

import net.minecraft.item.map.MapIcon;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.item.map.MapIconAccessor;

@Mixin(MapUpdateS2CPacket.class)
public class MapUpdateS2CPacketMixin {
    @Inject(method = "method_43883", at = @At(value = "RETURN"))
    private static void fwaystones$read_waystone_state(PacketByteBuf buf3, CallbackInfoReturnable<MapIcon> cir) {
        ((MapIconAccessor)cir.getReturnValue()).setIsWaystone(buf3.readBoolean());
    }

    @Inject(method = "method_34136", at = @At(value = "RETURN"))
    private static void fwaystones$write_waystone_state(PacketByteBuf b, MapIcon icon, CallbackInfo ci) {
        b.writeBoolean(((MapIconAccessor)icon).getIsWaystone());
    }
}
