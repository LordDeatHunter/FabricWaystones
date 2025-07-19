package wraith.fwaystones.mixin.scoreboard;

import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScoreboardState.class)
public abstract class ScoreboardStateMixin {

    @Final
    @Shadow
    private Scoreboard scoreboard;

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void wraithWaystones$writeAttachedData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfoReturnable<NbtCompound> cir) {
        ((AttachmentTargetImpl) scoreboard).fabric_writeAttachmentsToNbt(nbt, registryLookup);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void wraithWaystones$readAttachedData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfoReturnable<NbtCompound> cir) {
        ((AttachmentTargetImpl) scoreboard).fabric_readAttachmentsFromNbt(nbt, registryLookup);
    }
}
