package wraith.fwaystones.api.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.teleport.impl.WaystoneTeleportAction;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.util.Utils;

import java.util.UUID;

public interface TeleportAction {
    TeleportSource source();

    default boolean isFrom(TeleportSource source) {
        return source().equals(source);
    }

    default boolean isValid(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (isFrom(TeleportSource.VOID_TOTEM)) {
                if (livingEntity instanceof PlayerEntity player) {
                    var cooldown = WaystonePlayerData.getData(player).teleportCooldown();

                    if (cooldown > 0) {
                        var cooldownSeconds = Utils.df.format(cooldown / 20F);
                        player.sendMessage(TextUtils.translationWithArg(
                            "no_teleport_message.cooldown",
                            cooldownSeconds
                        ), false);
                        return false;
                    }
                }
            } else if(isFrom(TeleportSource.ABYSS_WATCHER)) {
                var stackReference = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(livingEntity, stack -> {
                    var component = stack.get(WaystoneDataComponents.TELEPORTER);

                    return component != null && component.oneTimeUse();
                });

                return stackReference != null;
            }
        }

        return true;
    }

    @Nullable
    TeleportTarget createTarget(Entity entity);

    GlobalPos getPos(World world);

    default int getCooldown() {
        var cooldowns = FabricWaystones.CONFIG.teleportCooldowns;
        return switch (source()) {
            case WAYSTONE -> cooldowns.usedWaystone();
            case ABYSS_WATCHER -> cooldowns.usedAbyssWatcher();
            case LOCAL_VOID -> cooldowns.usedLocalVoid();
            case VOID_TOTEM -> cooldowns.usedVoidTotem();
            case POCKET_WORMHOLE -> cooldowns.usedPockedWormhole();
        };
    }

    default void addConsumedItems(World world, Item item, int amount){}

    static WaystoneTeleportAction of(@Nullable UUID uuid, TeleportSource source) {
        return new WaystoneTeleportAction(uuid, source);
    }

}
