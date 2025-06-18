package wraith.fwaystones.api.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public sealed interface WaystoneType permits WaystoneTypeImpl {
    StructEndec<WaystoneType> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("particle_texture", WaystoneType::particleTexture),
            Endec.INT.fieldOf("default_rune_color", WaystoneType::defaultRuneColor),
            CodecUtils.toEndec(RegistryCodecs.entryList(RegistryKeys.ITEM)).fieldOf("material_item_tag", WaystoneType::materialItemTag),
            CodecUtils.toEndec(RegistryCodecs.entryList(RegistryKeys.BLOCK)).fieldOf("material_block_tag", WaystoneType::materialBlockTag),
            WaystoneType::of
    );

    static WaystoneType ofTags(Identifier particleTexture, int defaultRuneColor, Identifier materialItemTag, Identifier materialBlockTag) {
        return ofTags(particleTexture, defaultRuneColor, TagKey.of(RegistryKeys.ITEM, materialItemTag), TagKey.of(RegistryKeys.BLOCK, materialBlockTag));
    }

    static WaystoneType ofTags(Identifier particleTexture, int defaultRuneColor, TagKey<Item> materialItemTag, TagKey<Block> materialBlockTag) {
        return of(particleTexture, defaultRuneColor, Registries.ITEM.getOrCreateEntryList(materialItemTag), Registries.BLOCK.getOrCreateEntryList(materialBlockTag));
    }

    static WaystoneType ofEntry(Identifier particleTexture, int defaultRuneColor, Item item, Block block) {
        return of(particleTexture, defaultRuneColor, RegistryEntryList.of(Registries.ITEM::getEntry, item), RegistryEntryList.of(Registries.BLOCK::getEntry, block));
    }

    static WaystoneType of(Identifier particleTexture, int defaultRuneColor, RegistryEntryList<Item> materialItemTag, RegistryEntryList<Block> materialBlockTag) {
        return new WaystoneTypeImpl(particleTexture, defaultRuneColor, materialItemTag, materialBlockTag);
    }

    @Nullable
    default Identifier getId() {
        return WaystoneTypes.getId(this);
    }

    default Identifier blockTexture() {
        var id = WaystoneTypes.getId(this);

        if (id == null) throw new IllegalStateException("Unable to get the block texture for the waystone type as its not properly registered!");

        return id.withPath(s -> "block/" + s + "_waystone");
    }

    default Identifier itemTexture() {
        var id = WaystoneTypes.getId(this);

        if (id == null) throw new IllegalStateException("Unable to get the block texture for the waystone type as its not properly registered!");

        return id.withPath(s -> "item/" + s + "_waystone");
    }

    default String getTranslationKey() {
        var id = getId();
        return id.getNamespace() + "." + id.getPath() + ".waystone_name";
    }

    Identifier particleTexture();

    default Identifier getIconLocation() {
        return itemTexture().withPath(s -> "textures/" + s + ".png");
    }

    int defaultRuneColor();

    RegistryEntryList<Item> materialItemTag();

    RegistryEntryList<Block> materialBlockTag();
}
