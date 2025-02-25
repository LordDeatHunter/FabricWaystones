package wraith.fwaystones.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.ClientPlayerEntityMixinAccess;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.packets.RequestPlayerSyncPacket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityMixinAccess {

    @Override
    public void fabricWaystones$requestSync() {
        ClientPlayNetworking.send(new RequestPlayerSyncPacket());
    }

    @Override
    public ArrayList<String> fabricWaystones$getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        var discoveredWaystones = ((PlayerEntityMixinAccess) this).fabricWaystones$getDiscoveredWaystones();
        for (String hash : discoveredWaystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(FabricWaystones.WAYSTONE_STORAGE.getName(hash));
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
    public ArrayList<String> fabricWaystones$getHashesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        var discoveredWaystones = ((PlayerEntityMixinAccess) this).fabricWaystones$getDiscoveredWaystones();
        for (String hash : discoveredWaystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(hash);
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(Comparator.comparing(a -> FabricWaystones.WAYSTONE_STORAGE.getName(a)));
        return waystones;
    }

    @Override
    public int fabricWaystones$getDiscoveredCount() {
        return ((PlayerEntityMixinAccess) this).fabricWaystones$getDiscoveredCount();
    }

}