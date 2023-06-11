package wraith.fwaystones.integration.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class WaystoneEvents {
	public static final Event<UpdateWaystone> DISCOVER_WAYSTONE_EVENT = EventFactory.createLoop();
	public static final Event<ForgetAllWaystones> FORGET_ALL_WAYSTONES_EVENT = EventFactory.createLoop();
	public static final Event<RemoveWaystone> REMOVE_WAYSTONE_EVENT = EventFactory.createLoop();
	public static final Event<UpdateWaystone> RENAME_WAYSTONE_EVENT = EventFactory.createLoop();

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
