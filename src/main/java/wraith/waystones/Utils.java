package wraith.waystones;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Utils {

    public static final Random random = new Random(Calendar.getInstance().getTimeInMillis());
    public static int getRandomIntInRange(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static String generateWaystoneName(String id) {
        String out;
        if (!"".equals(id) && !Waystones.WAYSTONE_DATABASE.containsWaystone(id))
            out = id;
        else {
            do {
                out = generateUniqueId();
            } while(Waystones.WAYSTONE_DATABASE.containsWaystone(out));
        }
        return out;
    }

    private static String generateUniqueId() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Character> vowels = new ArrayList<Character>(){{
            add('a');
            add('e');
            add('i');
            add('o');
            add('u');
        }};
        char c;
        do {
            c = (char)Utils.getRandomIntInRange(65, 90);
        } while(c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U');
        sb.append(c);
        sb.append(vowels.get(Utils.random.nextInt(5)));
        for (int i = 0; i < 3; ++i) {
            do {
                c = (char)Utils.getRandomIntInRange(97, 122);
            } while(c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
            sb.append(c);
            sb.append(vowels.get(Utils.random.nextInt(5)));
        }
        return sb.toString();
    }


    public static StructurePool tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            ModifiableStructurePool modPool = new ModifiableStructurePool(pool);
            modPool.addStructurePoolElement(StructurePoolElement.method_30426(elementId, StructureProcessorLists.EMPTY).apply(projection), weight);
            return modPool.getStructurePool();
        }
        return pool;
    }
}
