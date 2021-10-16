package wraith.waystones.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

@FunctionalInterface
public interface ServerPlayerEntityTeleporter {

    public void teleport(ServerPlayerEntity player, ServerWorld world, TeleportTarget target);

}
