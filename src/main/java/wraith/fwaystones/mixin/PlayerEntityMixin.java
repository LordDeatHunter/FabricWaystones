package wraith.fwaystones.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystonePlayerData;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique
    private PlayerEntity _this() {
        return (PlayerEntity) (Object) this;
    }

    //--

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        var data = WaystonePlayerData.getData(_this());

        var teleportCooldown = data.teleportCooldown();

        if (teleportCooldown <= 0) return;

        data.teleportCooldown(Math.max(0, teleportCooldown - 1));
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        WaystonePlayerData.getData(_this()).teleportCooldown(FabricWaystones.CONFIG.teleportation_cooldown.cooldown_ticks_when_hurt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        if (!tag.contains(FabricWaystones.MOD_ID)) return;

        tag = tag.getCompound(FabricWaystones.MOD_ID);

        WaystonePlayerData.setData(_this(), tag);
    }
}
