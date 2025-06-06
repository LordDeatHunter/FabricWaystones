package wraith.fwaystones.mixin.xaeros;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.integration.xaeros.XaerosMinimapCompat;
import xaero.common.minimap.waypoints.Waypoint;

import java.util.UUID;

@Pseudo
@Mixin(Waypoint.class)
public class WaypointMixin {

    @Shadow private String name;

    @Inject(method = "getLocalizedName", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void fabricWaystones$xaerosNameReplacement(CallbackInfoReturnable<String> cir) {
        var alternativeText = getWaystoneText(name);
        if (alternativeText != null) cir.setReturnValue(alternativeText.getString());
    }

    @Inject(method = "setName", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void fabricWaystones$xaerosNameReplacement(String name, CallbackInfo ci) {
        var alternativeText = getWaystoneText(name);
        if (alternativeText != null) ci.cancel();
    }

    @Unique
    private static @Nullable Text getWaystoneText(String string) {
        var storage = XaerosMinimapCompat.getStorage();

        if (storage == null || !string.contains("[Wraith Waystone: ")) return null;

        var parts = string.split("\\[Wraith Waystone: ");

        if (parts.length == 1) {
            var uuidStr = parts[0].replace("]", "");

            var uuid = UUID.fromString(uuidStr);

            if (storage.hasData(uuid)) {
                var data = storage.getData(uuid);

                return data.name();
            }
        } else if (parts.length > 1) {
            var start = parts[0];
            var subParts = parts[1].split("]");

            if (subParts.length == 0) return null;

            var uuidStr = subParts[0];

            var uuid = UUID.fromString(uuidStr);

            if (!storage.hasData(uuid)) return null;

            var baseText = Text.literal(start);

            var data = storage.getData(uuid);

            baseText.append(data.name());

            for (int i = 1; i < subParts.length; i++) {
                baseText.append(subParts[i])
                        .append("]");
            }

            return baseText;
        }

        return null;
    }
}
