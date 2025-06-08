package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.item.components.WaystoneHashTarget;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.s2c.VoidRevive;
import wraith.fwaystones.util.TeleportSources;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.ArrayList;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(method = "tryUseTotem")
    public boolean revive(DamageSource source, Operation<Boolean> original) {
        if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
            ItemStack stack = WaystoneComponentEventHooks.getVoidTotem(player);

            if (stack != null) {
                player.setHealth(1.0F);
                player.clearStatusEffects();
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                var teleported = false;

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    WaystoneNetworkHandler.CHANNEL.serverHandle(serverPlayer).send(new VoidRevive());
                    var world = serverPlayer.getWorld();
                    // Try to get the stored waystone
                    var target = WaystoneHashTarget.get(stack, world);
                    var uuid = target != null ? target.uuid() : null;
                    if (uuid == null) {
                        // If no such waystone exists, get a random discovered waystone
                        var discovered = WaystonePlayerData.getData(player).discoveredWaystones();
                        if (!discovered.isEmpty()) {
                            var list = new ArrayList<>(discovered);

                            uuid = list.get(Utils.random.nextInt(list.size()));
                        }
                    }
                    if (uuid != null) {
                        var waystone = WaystoneDataStorage.getStorage(world).getEntity(uuid);
                        if (waystone != null) {
                            player.fallDistance = 0;
                            waystone.teleportPlayer(player, false, TeleportSources.VOID_TOTEM);
                            teleported = true;
                        }
                    }
                }

                return teleported || !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
            }
        }

        return original.call(source);
    }

}
