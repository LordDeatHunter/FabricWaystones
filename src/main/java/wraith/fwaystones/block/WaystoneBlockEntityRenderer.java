package wraith.fwaystones.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.registry.ItemRegistry;

public class WaystoneBlockEntityRenderer implements BlockEntityRenderer<WaystoneBlockEntity, WaystoneBlockEntityRenderer.WaystoneRenderState> {

    private final ItemModelResolver itemModelResolver;

    public WaystoneBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public WaystoneRenderState createRenderState() {
        return new WaystoneRenderState();
    }

    @Override
    public void extractRenderState(WaystoneBlockEntity entity, WaystoneRenderState state, float tickDelta, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.active = entity.getBlockState().getValue(WaystoneBlock.ACTIVE);
        state.rotation = entity.lookingRotR;
        if (entity.getLevel() != null) {
            state.lightAbove = LevelRenderer.getLightColor(
                LevelRenderer.BrightnessGetter.DEFAULT,
                entity.getLevel(),
                entity.getBlockState(),
                entity.getBlockPos().above()
            );
        }
        this.itemModelResolver.updateForTopItem(
            state.itemState,
            new ItemStack(ItemRegistry.get("abyss_watcher")),
            ItemDisplayContext.FIXED,
            entity.getLevel(),
            null,
            (int) entity.getBlockPos().asLong()
        );
    }

    @Override
    public void submit(WaystoneRenderState state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!state.active || state.itemState.isEmpty()) {
            return;
        }
        matrices.pushPose();
        matrices.scale(0.5f, 0.5f, 0.5f);
        matrices.translate(1f, 3.5f, 1f);

        matrices.mulPose(Axis.YN.rotationDegrees(state.rotation));
        matrices.mulPose(Axis.YP.rotationDegrees(90));
        state.itemState.submit(matrices, collector, state.lightAbove, OverlayTexture.NO_OVERLAY, 0);

        matrices.popPose();
    }

    public static class WaystoneRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState itemState = new ItemStackRenderState();
        public boolean active;
        public float rotation;
        public int lightAbove;
    }

}
