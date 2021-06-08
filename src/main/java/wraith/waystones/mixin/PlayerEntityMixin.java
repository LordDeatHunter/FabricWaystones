package wraith.waystones.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Utils;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityMixinAccess {

    private final HashSet<String> discoveredWaystones = new HashSet<>();

    @Override
    public void discoverWaystone(WaystoneBlockEntity waystone) {
        discoveredWaystones.add(waystone.getHash());
        syncData();
    }

    @Override
    public boolean hasDiscoveredWaystone(WaystoneBlockEntity waystone) {
        return discoveredWaystones.contains(waystone.getHash());
    }

    @Override
    public void forgetWaystone(WaystoneBlockEntity waystone) {
        discoveredWaystones.remove(waystone.getHash());
        syncData();
    }

    @Override
    public void forgetWaystone(String hash) {
        discoveredWaystones.remove(hash);
        syncData();
    }

    @Override
    public void syncData() {
        if (((PlayerEntity)(Object)this).world.isClient) {
            return;
        }
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
        packet.writeCompoundTag(toTagW(new CompoundTag()));
        ServerPlayNetworking.send((ServerPlayerEntity)(Object)this, Utils.ID("sync_player"), packet);
    }

    @Override
    public HashSet<String> getDiscoveredWaystones() {
        return discoveredWaystones;
    }

    @Override
    public int getDiscoveredAmount() {
        return discoveredWaystones.size();
    }

    @Override
    public ArrayList<String> getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(Waystones.WAYSTONE_STORAGE.getWaystone(hash).getName());
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(String::compareTo);
        return waystones;
    }

    @Override
    public ArrayList<String> getHashesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(hash);
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(Comparator.comparing(a -> Waystones.WAYSTONE_STORAGE.getWaystone(a).getName()));
        return waystones;
    }


    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        toTagW(tag);
    }

    @Override
    public CompoundTag toTagW(CompoundTag tag) {
        ListTag waystones = new ListTag();
        for(String waystone : discoveredWaystones) {
            waystones.add(StringTag.of(waystone));
        }
        tag.put("discovered_waystones", waystones);
        return tag;
    }

    @Override
    public void learnWaystones(PlayerEntity player, boolean overwrite) {
        discoveredWaystones.clear();
        this.discoveredWaystones.addAll(((PlayerEntityMixinAccess)player).getDiscoveredWaystones());
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        fromTagW(tag);
    }

    @Override
    public void fromTagW(CompoundTag tag) {
        if (!tag.contains("discovered_waystones")) {
            return;
        }
        discoveredWaystones.clear();
        ListTag waystones = tag.getList("discovered_waystones", 8);
        for (Tag waystone : waystones) {
            discoveredWaystones.add(waystone.asString());
        }
    }

}
