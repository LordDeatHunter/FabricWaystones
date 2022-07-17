package wraith.fwaystones.mixin;

import com.google.common.collect.Maps;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.item.map.MapIconAccessor;
import wraith.fwaystones.item.map.MapStateAccessor;
import wraith.fwaystones.item.map.MapWaystoneMarker;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Mixin(MapState.class)
public class MapStateMixin implements MapStateAccessor {

    @Shadow
    private void removeIcon(String id) {}

    @Shadow
    private void addIcon(MapIcon.Type type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {}

    @Unique
    private final Map<String, MapWaystoneMarker> fwaystones$waystones = Maps.newHashMap();

    @Unique
    private boolean fwaystones$is_adding_waystone_icon;

    @Inject(method = "fromNbt", at = @At("TAIL"))
    private static void loadWaystonesNbt(NbtCompound nbt, CallbackInfoReturnable<MapState> cir) {
        MapState mapState = cir.getReturnValue();
        NbtList nbtList = nbt.getList("waystones", NbtElement.COMPOUND_TYPE);
        for (int k = 0; k < nbtList.size(); ++k) {
            MapWaystoneMarker mapWaystoneMarker = MapWaystoneMarker.fromNbt(nbtList.getCompound(k));
            ((MapStateMixin)(Object)mapState).fwaystones$waystones.put(mapWaystoneMarker.getKey(), mapWaystoneMarker);
            ((MapStateMixin)(Object)mapState).fwaystones$is_adding_waystone_icon = true;
            ((MapStateMixin)(Object)mapState).addIcon(mapWaystoneMarker.getIconType(), null, mapWaystoneMarker.getKey(), mapWaystoneMarker.getPos().getX(), mapWaystoneMarker.getPos().getZ(), 180.0, mapWaystoneMarker.getName());
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void saveWaystonesNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        NbtList nbtList = new NbtList();
        for (MapWaystoneMarker mapwaystoneMarker : this.fwaystones$waystones.values()) {
            nbtList.add(mapwaystoneMarker.getNbt());
        }
        nbt.put("waystones", nbtList);
    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Ljava/util/Map;putAll(Ljava/util/Map;)V", ordinal = 1))
    private void copyWaystones(CallbackInfoReturnable<MapState> cir) {
        ((MapStateMixin)(Object)cir.getReturnValue()).fwaystones$waystones.putAll(this.fwaystones$waystones);
    }

    @ModifyVariable(method = "addIcon", at = @At("STORE"))
    private MapIcon markIconAsWaystoneIcon(MapIcon icon) {
        if (fwaystones$is_adding_waystone_icon)
            ((MapIconAccessor)icon).setIsWaystone(true);
        fwaystones$is_adding_waystone_icon = false;
        return icon;
    }

    @Override
    public boolean addWaystone(WorldAccess world, BlockPos pos) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getZ() + 0.5;
        int i = 1 << ((MapState)(Object)this).scale;
        double f = (d - (double)((MapState)(Object)this).centerX) / (double)i;
        double g = (e - (double)((MapState)(Object)this).centerZ) / (double)i;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapWaystoneMarker mapWaystoneMarker = MapWaystoneMarker.fromWorldBlock(world, pos);
            if (mapWaystoneMarker == null) {
                return false;
            }
            if (this.fwaystones$waystones.remove(mapWaystoneMarker.getKey(), mapWaystoneMarker)) {
                removeIcon(mapWaystoneMarker.getKey());
                return true;
            }
            if (!((MapState)(Object)this).method_37343(256)) {
                this.fwaystones$waystones.put(mapWaystoneMarker.getKey(), mapWaystoneMarker);
                fwaystones$is_adding_waystone_icon = true;
                addIcon(mapWaystoneMarker.getIconType(), world, mapWaystoneMarker.getKey(), d, e, 180.0, mapWaystoneMarker.getName());
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"TooBroadScope", "UnclearExpression"})
    @Override
    public @Nullable BlockPos removeWaystone(BlockView world, int x, int z) {
        Iterator<MapWaystoneMarker> iterator = this.fwaystones$waystones.values().iterator();
        while (iterator.hasNext()) {
            MapWaystoneMarker mapWaystoneMarker2;
            MapWaystoneMarker mapWaystoneMarker = iterator.next();
            if (mapWaystoneMarker.getPos().getX() != x || mapWaystoneMarker.getPos().getZ() != z || mapWaystoneMarker.equals(mapWaystoneMarker2 = MapWaystoneMarker.fromWorldBlock(world, mapWaystoneMarker.getPos()))) continue;
            iterator.remove();
            this.removeIcon(mapWaystoneMarker.getKey());
            return mapWaystoneMarker.getPos();
        }
        return null;
    }

    @Override
    public Collection<MapWaystoneMarker> getFwaystones$waystones() {
        return this.fwaystones$waystones.values();
    }
}