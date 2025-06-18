package wraith.fwaystones.api.client;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColorProvider;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.core.MossType;
import wraith.fwaystones.api.core.MossTypes;

import java.util.HashMap;
import java.util.Map;

public class MossColorProvidersRegistry {

    private static final Map<MossType, BlockColorProvider> COLOR_PROVIDER_MAP = new HashMap<>();

    public static void registerProvider(MossType type, BlockColorProvider provider) {
        if (COLOR_PROVIDER_MAP.containsKey(type)) {
            throw new IllegalStateException("Unable to register MossType with provider as the given type already has a provider: " + type);
        }

        COLOR_PROVIDER_MAP.put(type, provider);
    }

    @Nullable
    public static BlockColorProvider getProvider(MossType type) {
        return COLOR_PROVIDER_MAP.get(type);
    }

    static {
        registerProvider(MossTypes.GRASS_TYPE, (state, world, pos, tintIndex) -> {
            var provider = ColorProviderRegistry.BLOCK.get(Blocks.GRASS_BLOCK);

            if (provider != null) {
                return provider.getColor(state, world, pos, tintIndex);
            }

            return -1;
        });
    }
}
