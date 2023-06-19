package wraith.fwaystones.mixin;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.item.VoidTotem;
import wraith.fwaystones.registry.ItemRegister;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.TeleportSources;
import wraith.fwaystones.util.Utils;

import java.util.ArrayList;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand hand);

	@Shadow
	public abstract void setHealth(float health);

	@Shadow
	public abstract boolean removeAllEffects();

	@Shadow
	public abstract boolean addEffect(MobEffectInstance effect);

	@Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
	public void revive(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (!(_this() instanceof Player player)) {
			return;
		}
		ItemStack stack = null;
		for (InteractionHand hand : InteractionHand.values()) {
			var currentStack = getItemInHand(hand);
			if (currentStack.getItem() != ItemRegister.VOID_TOTEM.get()) continue;
			stack = currentStack.copy();
			currentStack.shrink(1);
			break;
		}
		if (stack == null) {
			return;
		}
		setHealth(1.0F);
		removeAllEffects();
		addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
		addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
		addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
		var teleported = false;
		if (player instanceof ServerPlayer serverPlayer) {
			FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
			NetworkManager.sendToPlayer(serverPlayer, PacketHandler.VOID_REVIVE, packet);
			// Try to get the stored waystone
			var hash = VoidTotem.getBoundWaystone(stack);
			if (hash == null) {
				// If no such waystone exists, get a random discovered waystone
				var discovered = ((PlayerEntityMixinAccess) player).getDiscoveredWaystones();
				if (!discovered.isEmpty()) {
					var list = new ArrayList<>(discovered);
					hash = list.get(Utils.random.nextInt(list.size()));
				}
			}
			if (hash != null) {
				var waystone = Waystones.STORAGE.getWaystoneEntity(hash);
				if (waystone != null) {
					player.fallDistance = 0;
					waystone.teleportPlayer(player, false, TeleportSources.VOID_TOTEM);
					teleported = true;
				}
			}
		}
		cir.setReturnValue(teleported || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));
		cir.cancel();
	}

	private LivingEntity _this() {
		return (LivingEntity) (Object) this;
	}

}
