package wraith.fwaystones.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.item.render.WaystoneCompassItemRenderer;
import wraith.fwaystones.registry.WaystoneItems;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @WrapMethod(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V")
    private void threadLocalThatLivingEntity(
        LivingEntity entity,
        ItemStack item,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int light,
        int overlay,
        int seed,
        Operation<Void> original
    ) {
        WaystoneCompassItemRenderer.livingHolder.set(entity);
        original.call(entity, item, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
        WaystoneCompassItemRenderer.livingHolder.remove();
    }
}
