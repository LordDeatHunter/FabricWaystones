package wraith.fwaystones.mixin.xaeros;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.integration.xaeros.XaerosMinimapCompat;
import xaero.common.misc.Misc;

import java.util.UUID;

@Pseudo
@Mixin(Misc.class)
public class MiscMixin {

    @WrapOperation(
            method = {
                    "drawNormalText(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFIZLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V",
                    "drawPiercingText(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFIZLnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I")
    )
    private static int fabricWaystones$hookIntoTextRendering(TextRenderer instance, String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light, Operation<Integer> original) {
        var alternativeText = getWaystoneText(text);

        if(alternativeText != null) {
            instance.draw(alternativeText, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
        } else {
            original.call(instance, text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
        }

        return color;
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
