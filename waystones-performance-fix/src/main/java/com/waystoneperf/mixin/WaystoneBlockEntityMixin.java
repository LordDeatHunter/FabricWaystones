package com.waystoneperf.mixin;

import com.waystoneperf.util.TeleportationOptimizer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept Fabric Waystones teleportation and inject performance optimizations.
 * This targets the WaystoneBlockEntity class from the fwaystones mod.
 */
@Mixin(targets = "wraith.fwaystones.block.WaystoneBlockEntity", remap = false)
public abstract class WaystoneBlockEntityMixin {

    @Shadow
    protected BlockPos pos;

    /**
     * Intercepts the teleportPlayer method to use our optimized teleportation system.
     *
     * This cancels the original teleport logic and queues it through TeleportationOptimizer
     * which handles async chunk loading and tick spreading.
     */
    @Inject(
        method = "teleportPlayer(Lnet/minecraft/entity/player/PlayerEntity;ZLwraith/fwaystones/util/TeleportSources;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleportTo(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            shift = At.Shift.BEFORE
        ),
        cancellable = true,
        remap = false
    )
    private void onTeleportPlayer(Object player, boolean takeCost, Object source, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        // Get the target world (this BlockEntity's world)
        ServerWorld targetWorld = (ServerWorld) ((net.minecraft.block.entity.BlockEntity) this).getWorld();
        if (targetWorld == null) {
            return;
        }

        // Calculate target position based on waystone facing
        // This mirrors the logic in WaystoneBlockEntity
        BlockPos targetPos = this.pos;
        Vec3d targetVec = new Vec3d(
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5
        );

        BlockPos oldPos = serverPlayer.getBlockPos();
        ServerWorld oldWorld = serverPlayer.getServerWorld();

        // Play departure sound immediately
        oldWorld.playSound(
            null,
            oldPos,
            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
            SoundCategory.BLOCKS,
            1.0F,
            1.0F
        );

        // Queue teleport through optimizer
        TeleportationOptimizer.getInstance().queueTeleport(
            serverPlayer,
            targetWorld,
            targetVec,
            serverPlayer.getYaw(),
            0.0F, // pitch
            () -> {
                // Callback after successful teleport
                BlockPos newPos = serverPlayer.getBlockPos();

                // Play arrival sound if teleported far or to different dimension
                if (!oldPos.isWithinDistance(newPos, 6) ||
                    !oldWorld.getRegistryKey().equals(targetWorld.getRegistryKey())) {

                    serverPlayer.getServer().execute(() -> {
                        targetWorld.playSound(
                            null,
                            newPos,
                            SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                            SoundCategory.BLOCKS,
                            1.0F,
                            1.0F
                        );
                    });
                }
            }
        );

        // Cancel original teleportation - we handle it through the optimizer
        cir.setReturnValue(true);
    }
}
