package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;

import java.util.*;

public class MossTypes {

    public static final Identifier EMPTY_ID = FabricWaystones.id("empty");

    public static final Registry<MossType> REGISTRY = FabricRegistryBuilder.<MossType>createDefaulted(RegistryKey.ofRegistry(FabricWaystones.id("moss")), EMPTY_ID)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Endec<MossType> ENDEC = MinecraftEndecs.IDENTIFIER.xmap(MossTypes::getTypeOrDefault, MossTypes::getIdOrDefault);

    public static MossType register(Identifier id, MossType type) {
        return Registry.register(REGISTRY, id, type);
    }

    @Nullable
    public static MossType getType(Identifier id) {
        return REGISTRY.getOrEmpty(id).orElse(null);
    }

    public static MossType getTypeOrDefault(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Identifier getId(MossType type) {
        return REGISTRY.getId(type);
    }

    public static Identifier getIdOrDefault(MossType type) {
        var id = getId(type);
        return id != null ? id : MossTypes.EMPTY_ID;
    }

    public static List<Identifier> getTypeIds() {
        var mosses = new HashSet<>(REGISTRY.getIds());

        mosses.remove(EMPTY_ID);

        return mosses.stream().sorted(Identifier::compareTo).toList();
    }

    public static List<MossType> getTypes() {
        return REGISTRY.getEntrySet().stream()
                .filter(entry -> !entry.getKey().getValue().equals(EMPTY_ID))
                .sorted(Comparator.comparing(entry -> entry.getKey().getValue(), Identifier::compareTo))
                .map(Map.Entry::getValue)
                .toList();
    }

    @Nullable
    public static MossType getMossType(ItemStack stack) {
        for (var type : REGISTRY) {
            if (stack.isIn(type.items())) {
                return type;
            }
        }

        return null;
    }

    public static final MossType EMPTY = register(EMPTY_ID, MossType.EMPTY);

    public static final Identifier CHORUS = FabricWaystones.id("chorus");
    public static final MossType CHORUS_TYPE = register(CHORUS, MossType.ofEntry(Items.CHORUS_FRUIT));

    public static final Identifier CRIMSON = FabricWaystones.id("crimson");
    public static final MossType CRIMSON_TYPE = register(CRIMSON, MossType.ofEntry(Items.WEEPING_VINES));

    public static final Identifier GRASS = FabricWaystones.id("grass");
    public static final MossType GRASS_TYPE = register(GRASS, MossType.ofEntry(Items.GRASS_BLOCK));

    public static final Identifier LICHEN = FabricWaystones.id("lichen");
    public static final MossType LICHEN_TYPE = register(LICHEN, MossType.ofEntry(Items.GLOW_LICHEN));

    public static final Identifier VINE = FabricWaystones.id("vine");
    public static final MossType VINE_TYPE = register(VINE, MossType.ofEntry(Items.VINE));

    public static final Identifier WARPED = FabricWaystones.id("warped");
    public static final MossType WARPED_TYPE = register(WARPED, MossType.ofEntry(Items.TWISTING_VINES));
}
