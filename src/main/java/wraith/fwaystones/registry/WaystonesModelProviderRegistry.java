package wraith.fwaystones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
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
                    NbtCompound tag = stack.getNbt();
                    return tag == null || !tag.contains("waystones") || tag.getList("waystones", 8).isEmpty() ? 0 : 1;
                } else if (stack.getItem() instanceof LocalVoidItem) {
                    NbtCompound tag = stack.getNbt();
                    return tag == null || !tag.contains("waystone") ? 0 : 1;
                }
                return 0f;
            }
        );
    }

}
