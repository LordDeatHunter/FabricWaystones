package wraith.fwaystones.api.moss;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public sealed interface MossType permits MossTypeImpl {

    MossType EMPTY = MossType.ofEntry();

    static MossType ofTags(TagKey<Item> tag) {
        return of(Registries.ITEM.getOrCreateEntryList(tag));
    }

    static MossType ofEntry(Item ...item) {
        return of(RegistryEntryList.of(Registries.ITEM::getEntry, item));
    }

    static MossType of(RegistryEntryList<Item> materialItemTag) {
        return new MossTypeImpl(materialItemTag);
    }

    @Nullable
    default Identifier getId() {
        return MossTypes.getId(this);
    }

    RegistryEntryList<Item> items();

    default int stateCount() {
        return 1;
    }

    default Identifier blockTexture() {
        var id = MossTypes.getId(this);

        if (id == null) throw new IllegalStateException("Unable to get the block texture for the waystone type as its not properly registered!");

        return id.withPath(s -> "block/waystone/" + s + "_moss");
    }
}

record MossTypeImpl(RegistryEntryList<Item> items) implements MossType { }
