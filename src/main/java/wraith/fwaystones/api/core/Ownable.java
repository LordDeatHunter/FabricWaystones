package wraith.fwaystones.api.core;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Ownable {
    UUID ownerID();

    String ownerName();

    void owner(@Nullable PlayerEntity player);
}
