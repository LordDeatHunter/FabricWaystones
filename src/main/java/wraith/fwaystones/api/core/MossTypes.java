package wraith.fwaystones.api.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;

import java.util.*;

public class MossTypes {

    public static final Identifier NO_MOSS_ID = FabricWaystones.id("no_moss");

    private static final List<Identifier> ALL_TYPE_IDS = new ArrayList<>();
    private static final BiMap<Identifier, MossType> TYPES = HashBiMap.create();

    public static final Endec<MossType> ENDEC = MinecraftEndecs.IDENTIFIER.xmap(MossTypes::getTypeOrDefault, MossTypes::getIdOrDefault);

    public static MossType registerType(Identifier id, MossType type) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Unable to add the given MossType as it was already registered with the given id: " + id);
        }

        TYPES.put(id, type);

        ALL_TYPE_IDS.add(id);

        return type;
    }

    @Nullable
    public static MossType getType(Identifier id) {
        return TYPES.get(id);
    }

    public static MossType getTypeOrDefault(Identifier id) {
        return TYPES.getOrDefault(id, MossTypes.VINE_TYPE);
    }

    public static Identifier getId(MossType type) {
        return TYPES.inverse().get(type);
    }

    public static Identifier getIdOrDefault(MossType type) {
        return TYPES.inverse().getOrDefault(type, VINE);
    }

    public static List<Identifier> getTypeIds() {
        return Collections.unmodifiableList(ALL_TYPE_IDS);
    }

    public static List<MossType> getTypes() {
        return ALL_TYPE_IDS.stream().map(MossTypes::getType).toList();
    }

    @Nullable
    public static MossType getMossType(ItemStack stack) {
        for (var value : TYPES.values()) {
            if (stack.isIn(value.items())) {
                return value;
            }
        }

        return null;
    }

    public static final Identifier CHORUS = FabricWaystones.id("chorus");
    public static final MossType CHORUS_TYPE = registerType(CHORUS, MossType.ofEntry(Items.CHORUS_FRUIT));

    public static final Identifier CRIMSON = FabricWaystones.id("crimson");
    public static final MossType CRIMSON_TYPE = registerType(CRIMSON, MossType.ofEntry(Items.WEEPING_VINES));

    public static final Identifier GRASS = FabricWaystones.id("grass");
    public static final MossType GRASS_TYPE = registerType(GRASS, MossType.ofEntry(Items.GRASS_BLOCK));

    public static final Identifier LICHEN = FabricWaystones.id("lichen");
    public static final MossType LICHEN_TYPE = registerType(LICHEN, MossType.ofEntry(Items.GLOW_LICHEN));

    public static final Identifier VINE = FabricWaystones.id("vine");
    public static final MossType VINE_TYPE = registerType(VINE, MossType.ofEntry(Items.VINE));

    public static final Identifier WARPED = FabricWaystones.id("warped");
    public static final MossType WARPED_TYPE = registerType(WARPED, MossType.ofEntry(Items.TWISTING_VINES));
}
