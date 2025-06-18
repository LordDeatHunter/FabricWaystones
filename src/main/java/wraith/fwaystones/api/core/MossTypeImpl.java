package wraith.fwaystones.api.core;

import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntryList;

public record MossTypeImpl(RegistryEntryList<Item> items) implements MossType {
}
