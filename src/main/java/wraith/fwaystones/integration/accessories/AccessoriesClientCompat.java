package wraith.fwaystones.integration.accessories;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import wraith.fwaystones.registry.WaystoneItems;

public class AccessoriesClientCompat {
    public static void init() {
        AccessoriesRendererRegistry.registerRenderer(WaystoneItems.LOCAL_VOID, WristRenderer::new);
        AccessoriesRendererRegistry.registerRenderer(WaystoneItems.POCKET_WORMHOLE, WristRenderer::new);
        AccessoriesRendererRegistry.registerRenderer(WaystoneItems.ABYSS_WATCHER, WristRenderer::new);
    }

    public static class WristRenderer implements SimpleAccessoryRenderer {
        @Override
        public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, EntityModel<M> model, MatrixStack matrices) {
            if (!(model instanceof BipedEntityModel<M> bipedEntityModel)) return;

            var isRight = (reference.slot() % 2) == 0;

            if (isRight) {
                AccessoryRenderer.transformToModelPart(matrices, bipedEntityModel.rightArm, 0, -0.4, 1);
            } else {
                AccessoryRenderer.transformToModelPart(matrices, bipedEntityModel.leftArm, 0, -0.4, 1);
            }

            matrices.scale(0.35f, 0.35f, 0.35f);
            matrices.translate(0,0,0.05);
        }

        @Override
        public boolean shouldRenderInFirstPerson(Arm arm, ItemStack stack, SlotReference reference) {
            return true;
        }
    }
}
