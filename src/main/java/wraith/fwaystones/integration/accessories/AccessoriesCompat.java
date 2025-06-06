package wraith.fwaystones.integration.accessories;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.api.core.ExtendedStackReference;

public class AccessoriesCompat {
    public static void init() {
        WaystoneInteractionEvents.LOCATE_EQUIPMENT.register((player, predicate) -> {
            var capability = AccessoriesCapability.get(player);

            if (capability != null) {
                var ref = capability.getFirstEquipped(predicate);

                if (ref != null) {
                    var slotRef = ref.reference();

                    return ExtendedStackReference.of(slotRef::getStack, slotRef::setStack, stack -> {
                        AccessoriesAPI.breakStack(slotRef);
                    });
                }
            }

            return null;
        });
    }
}
