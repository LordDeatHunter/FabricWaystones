package wraith.fwaystones.api.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;

import java.util.*;

public class WaystoneTypes {

    public static final Identifier STONE = FabricWaystones.id("stone");

    public static final Registry<WaystoneType> REGISTRY = FabricRegistryBuilder.<WaystoneType>createDefaulted(RegistryKey.ofRegistry(FabricWaystones.id("waystone")), STONE)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Endec<WaystoneType> ENDEC = MinecraftEndecs.IDENTIFIER.xmap(WaystoneTypes::getTypeOrDefault, WaystoneTypes::getIdOrDefault);

    public static WaystoneType registerType(Identifier id, WaystoneType type) {
        return Registry.register(REGISTRY, id, type);
    }

    @Nullable
    public static WaystoneType getType(Identifier id) {
        return REGISTRY.getOrEmpty(id).orElse(null);
    }

    public static WaystoneType getTypeOrDefault(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Identifier getId(WaystoneType type) {
        return REGISTRY.getId(type);
    }

    public static Identifier getIdOrDefault(WaystoneType type) {
        return REGISTRY.getId(type);
    }

    public static List<Identifier> getTypeIds() {
        return REGISTRY.getIds().stream().sorted(Identifier::compareTo).toList();
    }

    public static List<WaystoneType> getTypes() {
        return REGISTRY.getEntrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getValue(), Identifier::compareTo))
                .map(Map.Entry::getValue)
                .toList();
    }

    public static final Identifier BLACKSTONE_BRICK = FabricWaystones.id("blackstone_brick");
    public static final WaystoneType BLACKSTONE_BRICK_TYPE = registerType(BLACKSTONE_BRICK, WaystoneType.ofTags(
            Identifier.of("block/polished_blackstone_bricks"),
            0xB4202A,
            common("bricks/blackstone"),
            common("bricks/blackstone")
    ));

    public static final Identifier DEEPSLATE_BRICK = FabricWaystones.id("deepslate_brick");
    public static final WaystoneType DEEPSLATE_BRICK_TYPE = registerType(DEEPSLATE_BRICK, WaystoneType.ofTags(
            Identifier.of("block/deepslate_bricks"),
            0xCF573C,
            common("bricks/deepslate"),
            common("bricks/deepslate")
    ));

    public static final Identifier DESERT = FabricWaystones.id("desert");
    public static final WaystoneType DESERT_TYPE = registerType(DESERT, WaystoneType.ofTags(
            Identifier.of("block/cut_sandstone"),
            0xF1641F,
            common("sandstone/uncolored_blocks"),
            common("sandstone/uncolored_blocks")
    ));

    public static final Identifier END_STONE_BRICK = FabricWaystones.id("end_stone_brick");
    public static final WaystoneType END_STONE_BRICK_TYPE = registerType(END_STONE_BRICK, WaystoneType.ofTags(
            Identifier.of("block/end_stone_bricks"),
            0x3B2754,
            common("bricks/endstone"),
            common("bricks/endstone")
    ));

    public static final Identifier NETHER_BRICK = FabricWaystones.id("nether_brick");
    public static final WaystoneType NETHER_BRICK_TYPE = registerType(NETHER_BRICK, WaystoneType.ofTags(
            Identifier.of("block/nether_bricks"),
            0xC4F129,
            common("bricks/nether"),
            common("bricks/nether")
    ));

    public static final Identifier RED_DESERT = FabricWaystones.id("red_desert");
    public static final WaystoneType RED_DESERT_TYPE = registerType(RED_DESERT, WaystoneType.ofTags(
            Identifier.of("block/cut_red_sandstone"),
            0x74DFED,
            common("sandstone/red_blocks"),
            common("sandstone/red_blocks")
    ));

    public static final Identifier RED_NETHER_BRICK = FabricWaystones.id("red_nether_brick");
    public static final WaystoneType RED_NETHER_BRICK_TYPE = registerType(RED_NETHER_BRICK, WaystoneType.ofTags(
            Identifier.of("block/red_nether_bricks"),
            0x67CB37,
            common("bricks/red_nether"),
            common("bricks/red_nether")
    ));

    public static final Identifier STONE_BRICK = FabricWaystones.id("stone_brick");
    public static final WaystoneType STONE_BRICK_TYPE = registerType(STONE_BRICK, WaystoneType.ofTags(
            Identifier.of("block/stone_bricks"),
            0xCC00FA,
            common("bricks/stone"),
            common("bricks/stone")
    ));


    public static final WaystoneType STONE_TYPE = registerType(STONE, WaystoneType.ofEntry(
            Identifier.of("block/stone"),
            0xC4F129,
            Items.STONE,
            Blocks.STONE
    ));

    private static Identifier common(String path) {
        return Identifier.of("c", path);
    }
}
