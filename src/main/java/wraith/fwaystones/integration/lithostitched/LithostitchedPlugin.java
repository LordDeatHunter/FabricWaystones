package wraith.fwaystones.integration.lithostitched;

import dev.worldgen.lithostitched.worldgen.poolelement.legacy.GuaranteedPoolElement;
import dev.worldgen.lithostitched.worldgen.poolelement.legacy.LimitedPoolElement;
import wraith.fwaystones.FabricWaystones;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * If Lithostitched is present, use its guaranteed/limited pool element system given it bypasses the vanilla system.
 *
 * @author Apollo
 */
public class LithostitchedPlugin {
    /**
     * Creates two elements: One to force waystones up to the min count, and one to limit them at the max count.
     * Not 1:1 with normal behavior (particularly in modded villages with few house variants) but it's still fairly close.
     */
    public static List<StructurePoolElement> createPieces(String name) {
        var config = FabricWaystones.CONFIG.worldgen;
        List<StructurePoolElement> elements = new ArrayList<>();

        if (config.min_per_village() > 0) {
            elements.add(new GuaranteedPoolElement(
                StructurePoolElement.single(name).apply(StructureTemplatePool.Projection.RIGID),
                Optional.empty(),
                config.min_per_village()
            ));
        }

        if (config.max_per_village() - config.min_per_village() > 0) {
            elements.add(new LimitedPoolElement(
                StructurePoolElement.single(name).apply(StructureTemplatePool.Projection.RIGID),
                Optional.empty(),
                config.max_per_village() - config.min_per_village()
            ));
        }

        return elements;
    }
}
