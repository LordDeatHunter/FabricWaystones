package wraith.fwaystones.integration.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class WaystoneEvents {
	public static Event<UpdateWaystone> DISCOVER_WAYSTONE_EVENT = EventFactory.createLoop(UpdateWaystone.class);
	public static Event<ForgetAllWaystones> FORGET_ALL_WAYSTONES_EVENT = EventFactory.createLoop(ForgetAllWaystones.class);
	public static Event<RemoveWaystone> REMOVE_WAYSTONE_EVENT = EventFactory.createLoop(RemoveWaystone.class);
	public static Event<UpdateWaystone> RENAME_WAYSTONE_EVENT = EventFactory.createLoop(UpdateWaystone.class);

	@FunctionalInterface
	public interface UpdateWaystone {

		void onUpdate(@Nullable String hash);
	}

	@FunctionalInterface
	public interface RemoveWaystone {

		void onRemove(@Nullable String hash);
	}

	@FunctionalInterface
	public interface ForgetAllWaystones {

		void onForgetAll(Player player);
	}
}
