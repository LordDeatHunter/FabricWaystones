package wraith.fwaystones.integration.accessories;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import net.minecraft.item.ItemStack;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.api.core.ExtendedStackReference;
import wraith.fwaystones.registry.WaystoneItems;

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

        var accessory = new Accessory() {
            @Override
            public boolean canEquipFromUse(ItemStack stack) {
                return false;
            }
        };
        AccessoriesAPI.registerAccessory(WaystoneItems.get("local_void"), accessory);
        AccessoriesAPI.registerAccessory(WaystoneItems.get("void_totem"), accessory);
        AccessoriesAPI.registerAccessory(WaystoneItems.get("pocket_wormhole"), accessory);
        AccessoriesAPI.registerAccessory(WaystoneItems.get("abyss_watcher"), accessory);
    }
}
