package wraith.fwaystones.api.core;

import net.minecraft.entity.player.PlayerEntity;

public interface WaystoneAccess {

    WaystonePosition position();

    boolean canAccess(PlayerEntity player);
}
