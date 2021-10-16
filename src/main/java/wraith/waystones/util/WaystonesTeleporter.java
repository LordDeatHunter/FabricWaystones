package wraith.waystones.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

import wraith.waystones.interfaces.ServerPlayerEntityTeleporter;

public class WaystonesTeleporter implements ServerPlayerEntityTeleporter {

    @Override
    public void teleport(ServerPlayerEntity player, ServerWorld targetWorld, TeleportTarget target) {
        player.teleport(targetWorld, target.position.x, target.position.y, target.position.z, target.yaw, target.pitch);
    }

}
