package wraith.waystones;

import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import wraith.waystones.mixin.StructurePoolAccess;

import java.util.List;

public class ModifiableStructurePool {

    private final StructurePool pool;

    public ModifiableStructurePool(StructurePool pool) {
        this.pool = pool;
    }

    public void addStructurePoolElement(StructurePoolElement element) {
        addStructurePoolElement(element, 1);
    }

    public void addStructurePoolElement(StructurePoolElement element, int weight) {
        for (int i = 0; i < weight; i++) {
            ((StructurePoolAccess)pool).getElements().add(element);
        }
        ((StructurePoolAccess)pool).getElementCounts().add(Pair.of(element, weight));
    }

    public List<StructurePoolElement> getElements() {
        return ((StructurePoolAccess)pool).getElements();
    }

    public StructurePool getStructurePool() {
        return pool;
    }
}