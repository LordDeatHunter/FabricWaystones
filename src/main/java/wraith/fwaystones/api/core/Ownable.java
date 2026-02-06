package wraith.fwaystones.api.core;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Ownable {
    UUID ownerID();

    String ownerName();

    void owner(@Nullable PlayerEntity player);

    default boolean isOwner(PlayerEntity player) {
        var owner = this.ownerID();

        return (owner != null && !player.getUuid().equals(owner));
    }
}
