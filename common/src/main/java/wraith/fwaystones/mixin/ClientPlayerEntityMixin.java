package wraith.fwaystones.mixin;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.ClientPlayerEntityMixinAccess;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.util.PacketHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityMixinAccess {
	@Override
	public void requestSync() {
		NetworkManager.sendToServer(PacketHandler.REQUEST_PLAYER_SYNC, new FriendlyByteBuf(Unpooled.buffer()));
	}
	@Override
	public ArrayList<String> getWaystonesSorted() {
		ArrayList<String> waystones = new ArrayList<>();
		HashSet<String> toRemove = new HashSet<>();
		var discoveredWaystones = ((PlayerEntityMixinAccess) this).getDiscoveredWaystones();
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
		var discoveredWaystones = ((PlayerEntityMixinAccess) this).getDiscoveredWaystones();
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
		return ((PlayerEntityMixinAccess) this).getDiscoveredCount();
	}
}
