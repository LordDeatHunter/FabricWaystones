package wraith.fwaystones.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public interface StructurePoolAccessor {

    @Accessor(value = "templates")
    ObjectArrayList<StructurePoolElement> getElements();

    @Accessor(value = "rawTemplates")
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Accessor(value = "rawTemplates")
    @Mutable
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> list);

}
