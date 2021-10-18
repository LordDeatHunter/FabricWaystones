package wraith.waystones.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

import wraith.waystones.interfaces.ServerPlayerEntityTeleporter;
import io.github.elbakramer.mc.teleportutils.util.TeleportUtils;

public class TeleportUtilsTeleporter implements ServerPlayerEntityTeleporter {

    @Override
    public void teleport(ServerPlayerEntity player, ServerWorld targetWorld, TeleportTarget target) {
        // let waystones do the particle and sound things
        TeleportUtils.teleportEntityWithItsPassengersLeashedAnimalsAndVehiclesRecursively(player, target, targetWorld,
                false, false);
    }

}
