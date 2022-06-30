package wraith.fwaystones.mixin;

import com.google.common.collect.Maps;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapFrameMarker;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.item.map.MapStateAccessor;
import wraith.fwaystones.item.map.MapWaystoneMarker;

import javax.security.auth.callback.Callback;
import java.util.Map;

@Mixin(MapState.class)
public class MapStateMixin implements MapStateAccessor {

    @Shadow
    private void removeIcon(String id) {}

    @Shadow
    private void addIcon(MapIcon.Type type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {}

    private final Map<String, MapWaystoneMarker> waystones = Maps.newHashMap();

    @Override
    public boolean addWaystone(WorldAccess world, BlockPos pos) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getZ() + 0.5;
        int i = 1 << ((MapState)(Object)this).scale;
        double f = (d - (double)((MapState)(Object)this).centerX) / (double)i;
        double g = (e - (double)((MapState)(Object)this).centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapWaystoneMarker mapWaystoneMarker = MapWaystoneMarker.fromWorldBlock(world, pos);
            if (mapWaystoneMarker == null) {
                return false;
            }
            if (this.waystones.remove(mapWaystoneMarker.getKey(), mapWaystoneMarker)) {
                removeIcon(mapWaystoneMarker.getKey());
                return true;
            }
            if (!((MapState)(Object)this).method_37343(256)) {
                this.waystones.put(mapWaystoneMarker.getKey(), mapWaystoneMarker);
                addIcon(mapWaystoneMarker.getIconType(), world, mapWaystoneMarker.getKey(), d, e, 180.0, mapWaystoneMarker.getName());
                return true;
            }
        }
        return false;
    }
}
