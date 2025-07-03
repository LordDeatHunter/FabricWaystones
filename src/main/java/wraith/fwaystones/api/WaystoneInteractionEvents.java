package wraith.fwaystones.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.core.ExtendedStackReference;

import java.util.function.Predicate;

public class WaystoneInteractionEvents {

    public static final Event<StackReferenceFinder> LOCATE_EQUIPMENT = EventFactory.createArrayBacked(
            StackReferenceFinder.class, callbacks -> (player, predicate) -> {
                for (var callback : callbacks) {
                    var ref = callback.getStack(player, predicate);

                    if (ref != null) {
                        return ref;
                    }
                }

                return null;
            }
    );

    public interface StackReferenceFinder {
        @Nullable ExtendedStackReference getStack(LivingEntity entity, Predicate<ItemStack> predicate);
    }
}
