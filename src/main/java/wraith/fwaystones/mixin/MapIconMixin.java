package wraith.fwaystones.mixin;

import net.minecraft.item.map.MapIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wraith.fwaystones.item.map.MapIconAccessor;

@Mixin(MapIcon.class)
public class MapIconMixin implements MapIconAccessor {
    @Unique
    private boolean fwaystones$isWaystone = false;

    @Override
    public MapIcon setIsWaystone(boolean bl) {
        fwaystones$isWaystone = bl;
        return (MapIcon)(Object)this;
    }

    @Override
    public boolean getIsWaystone() {
        return this.fwaystones$isWaystone;
    }
}
