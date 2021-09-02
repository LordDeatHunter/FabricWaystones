package wraith.waystones.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import wraith.waystones.mixin.StructurePoolAccess;

import java.util.ArrayList;
import java.util.List;

public record ModifiableStructurePool(StructurePool pool) {

    public void addStructurePoolElement(StructurePoolElement element) {
        addStructurePoolElement(element, 1);
    }

    public void addStructurePoolElement(StructurePoolElement element, int weight) {
        List<Pair<StructurePoolElement, Integer>> list = new ArrayList<>(((StructurePoolAccess) pool).getElementCounts());
        list.add(Pair.of(element, weight));
        ((StructurePoolAccess) pool).setElementCounts(list);
        for (int i = 0; i < weight; i++) {
            ((StructurePoolAccess) pool).getElements().add(element);
        }
    }

    public List<StructurePoolElement> getElements() {
        return ((StructurePoolAccess) pool).getElements();
    }

    public StructurePool getStructurePool() {
        return pool;
    }
}