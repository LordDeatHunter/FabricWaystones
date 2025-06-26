package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.networking.packets.s2c.UpdateMapStateWaystoneMarkers;
import wraith.fwaystones.pond.MapStateDuck;

import java.util.*;

import static wraith.fwaystones.networking.WaystoneNetworkHandler.CHANNEL;

@Mixin(MapState.class)
public abstract class MapStateMixin implements MapStateDuck {
    @Shadow @Final public RegistryKey<World> dimension;
    @Shadow @Final public byte scale;
    @Shadow @Final public int centerX;
    @Shadow @Final public int centerZ;

    @Shadow protected abstract void markDecorationsDirty();
    @Unique private List<UUID> waystoneMarkers = new ArrayList<>();

    @Unique private static final KeyedEndec<List<UUID>> WAYSTONE_MARKERS_ENDEC = BuiltInEndecs.UUID.listOf().keyed(FabricWaystones.MOD_ID + ":waystone_markers", new ArrayList<>());

    @ModifyReturnValue(method = "fromNbt", at = @At("RETURN"))
    private static MapState loadWaystoneMarkers(
        MapState mapState,
        @Local(argsOnly = true) NbtCompound nbt
    ) {
        ((MapStateMixin) (Object) mapState).waystoneMarkers = nbt.get(WAYSTONE_MARKERS_ENDEC);
        return mapState;
    }

    @ModifyReturnValue(method = "writeNbt", at = @At("RETURN"))
    private NbtCompound writeWaystoneMarkers(
        NbtCompound nbt
    ) {
        nbt.put(WAYSTONE_MARKERS_ENDEC, waystoneMarkers);
        return nbt;
    }

    @ModifyReturnValue(method = "copy", at = @At("RETURN"))
    private MapState copyWaystoneMarkers(
        MapState original
    ) {
        ((MapStateMixin)(Object)original).waystoneMarkers.addAll(this.waystoneMarkers);
        return original;
    }

    @Inject(method = "getPlayerMarkerPacket", at = @At("RETURN"))
    private void syncWaystoneMarkers(
        MapIdComponent mapId,
        PlayerEntity player,
        CallbackInfoReturnable<Packet<?>> cir
    ) {
        CHANNEL.serverHandle(player).send(new UpdateMapStateWaystoneMarkers(mapId, waystoneMarkers));
    }

    @Override
    public List<UUID> fwaystones$getWaystoneMarkers() {
        return Collections.unmodifiableList(this.waystoneMarkers);
    }

    @Override
    public boolean fwaystones$toggleWaystoneMarker(
        WaystoneData waystoneData,
        WaystonePosition position
    ) {
        if (waystoneData == null || position == null) return false;
        if (waystoneMarkers.remove(waystoneData.uuid())) return true;
        if (position.worldKey() != dimension) return false;
        var pos = position.blockPos();

        int i = 1 << scale;
        double xDistance = ((double) pos.getX() - centerX) / i;
        double zDistance = ((double) pos.getZ() - centerZ) / i;
        int maxDistance = 63;

        if (xDistance < -maxDistance ||
            zDistance < -maxDistance ||
            xDistance > maxDistance ||
            zDistance > maxDistance
        ) return false;

        waystoneMarkers.add(waystoneData.uuid());
        this.markDecorationsDirty();
        return true;
    }

    @Override
    public void fwaystones$setWaystoneMarkers(List<UUID> markers) {
        waystoneMarkers.clear();
        waystoneMarkers.addAll(markers);
    }
}
