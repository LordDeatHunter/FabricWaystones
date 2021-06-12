package wraith.waystones;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.HashMap;
import java.util.HashSet;

@Environment(EnvType.CLIENT)
public class ClientWaystoneStorage {

    private final HashMap<String, String> WAYSTONES = new HashMap<>();
    private final HashSet<String> GLOBALS = new HashSet<>();

    public void fromTag(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        if (tag.contains("waystones")) {
            loadWaystones(tag.getList("waystones", 10));
        }
        if (tag.contains("global_waystones")) {
            loadGlobals(tag.getList("global_waystones", 8));
        }
    }

    private void loadGlobals(ListTag globals) {
        GLOBALS.clear();
        for (int i = 0; i < globals.size(); ++i) {
            GLOBALS.add(globals.getString(i));
        }
    }

    private void loadWaystones(ListTag waystones) {
        WAYSTONES.clear();
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

    public HashSet<String> getGlobals() {
        return new HashSet<>(GLOBALS);
    }

    public int getHashCount() {
        return WAYSTONES.size();
    }

    public HashSet<String> getAllHashes() {
        return new HashSet<>(WAYSTONES.keySet());
    }

}
