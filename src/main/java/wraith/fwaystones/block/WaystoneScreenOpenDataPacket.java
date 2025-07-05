package wraith.fwaystones.block;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.api.core.WaystoneAccess;
import wraith.fwaystones.api.core.WaystonePosition;

public record WaystoneScreenOpenDataPacket(WaystonePosition position, boolean canUse) implements WaystoneAccess {

    public static final Endec<WaystoneScreenOpenDataPacket> ENDEC = StructEndecBuilder.of(
            WaystonePosition.ENDEC.fieldOf("hash", WaystoneScreenOpenDataPacket::position),
            Endec.BOOLEAN.fieldOf("can_use", WaystoneScreenOpenDataPacket::canUse),
            WaystoneScreenOpenDataPacket::new
    );

    @Override
    public boolean canAccess(PlayerEntity player) {
        return canUse;
    }
}
