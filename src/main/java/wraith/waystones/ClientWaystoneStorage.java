package wraith.waystones;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ClientWaystoneStorage {

    private final HashMap<String, String> WAYSTONES = new HashMap<>();

    public void fromTag(CompoundTag tag) {
        if (tag == null || !tag.contains("waystones")) {
            return;
        }
        WAYSTONES.clear();
        ListTag waystones = tag.getList("waystones", 10);

        for (int i = 0; i < waystones.size(); ++i) {
            CompoundTag waystoneTag = waystones.getCompound(i);
            if (!waystoneTag.contains("hash") || !waystoneTag.contains("name")) {
                continue;
            }
            String hash = waystoneTag.getString("hash");
            String name = waystoneTag.getString("name");
            WAYSTONES.put(hash, name);
        }
    }

    public String getName(String waystone) {
        return WAYSTONES.getOrDefault(waystone, null);
    }

    public boolean containsWaystone(String hash) {
        return WAYSTONES.containsKey(hash);
    }

}
