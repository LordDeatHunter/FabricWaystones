package wraith.waystones.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import wraith.waystones.Waystones;
import wraith.waystones.access.ClientPlayerEntityMixinAccess;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.util.WaystonePacketHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityMixinAccess {

    @Override
    public void requestSync() {
        ClientPlayNetworking.send(WaystonePacketHandler.REQUEST_PLAYER_SYNC, PacketByteBufs.create());
    }

    @Override
    public ArrayList<String> getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        HashSet<String> discoveredWaystones = ((PlayerEntityMixinAccess)this).getDiscoveredWaystones();
        for (String hash : discoveredWaystones) {
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(Waystones.WAYSTONE_STORAGE.getName(hash));
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
        HashSet<String> discoveredWaystones = ((PlayerEntityMixinAccess)this).getDiscoveredWaystones();
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

        waystones.sort(Comparator.comparing(a -> Waystones.WAYSTONE_STORAGE.getName(a)));
        return waystones;
    }

    @Override
    public int getDiscoveredCount() {
        return ((PlayerEntityMixinAccess)this).getDiscoveredCount();
    }

}