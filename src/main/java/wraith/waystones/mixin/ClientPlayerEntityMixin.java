package wraith.waystones.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import wraith.waystones.interfaces.ClientPlayerEntityMixinAccess;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.util.Utils;
import wraith.waystones.client.WaystonesClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityMixinAccess {

    @Override
    public void requestSync() {
        ClientPlayNetworking.send(Utils.ID("request_player_waystone_update"), new PacketByteBuf(Unpooled.buffer()));
    }

    @Override
    public ArrayList<String> getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        HashSet<String> discoveredWaystones = ((PlayerEntityMixinAccess)this).getDiscoveredWaystones();
        for (String hash : discoveredWaystones) {
            if (WaystonesClient.WAYSTONE_STORAGE.containsWaystone(hash)) {
                waystones.add(WaystonesClient.WAYSTONE_STORAGE.getName(hash));
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
            if (WaystonesClient.WAYSTONE_STORAGE.containsWaystone(hash)) {
                waystones.add(hash);
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(Comparator.comparing(a -> WaystonesClient.WAYSTONE_STORAGE.getName(a)));
        return waystones;
    }

    @Override
    public int getDiscoveredCount() {
        return ((PlayerEntityMixinAccess)this).getDiscoveredCount();
    }

}