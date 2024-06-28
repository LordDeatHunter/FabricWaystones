package wraith.fwaystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.item.WaystoneScrollItem;
import wraith.fwaystones.util.Utils;

@Environment(EnvType.CLIENT)
public final class WaystonesModelProviderRegistry {

    public static void register() {
        ModelPredicateProviderRegistry.register(Utils.ID("has_learned"),
            (stack, world, entity, seed) -> {
                if (stack.isEmpty()) {
                    return 0f;
                }
                if (stack.getItem() instanceof WaystoneScrollItem) {
                    return 1f;
                } else if (stack.getItem() instanceof LocalVoidItem) {
                    return 1f;
                }
                return 0f;
            }
        );
    }

}
