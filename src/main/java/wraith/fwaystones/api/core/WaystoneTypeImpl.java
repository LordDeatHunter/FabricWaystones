package wraith.fwaystones.api.core;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public record WaystoneTypeImpl(Identifier particleTexture, int defaultRuneColor, RegistryEntryList<Item> materialItemTag, RegistryEntryList<Block> materialBlockTag) implements WaystoneType {

}
