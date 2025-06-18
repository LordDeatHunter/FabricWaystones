package wraith.fwaystones.api.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;

import java.util.*;

public class WaystoneTypes {

    private static final List<Identifier> ALL_TYPE_IDS = new ArrayList<>();
    private static final BiMap<Identifier, WaystoneType> TYPES = HashBiMap.create();

    public static final Endec<WaystoneType> ENDEC = MinecraftEndecs.IDENTIFIER.xmap(WaystoneTypes::getTypeOrDefault, WaystoneTypes::getIdOrDefault);

    public static WaystoneType registerType(Identifier id, WaystoneType type) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Unable to add the given WaystoneType as it was already registered with the given id: " + id);
        }

        TYPES.put(id, type);

        ALL_TYPE_IDS.add(id);

        return type;
    }

    @Nullable
    public static WaystoneType getType(Identifier id) {
        return TYPES.get(id);
    }

    public static WaystoneType getTypeOrDefault(Identifier id) {
        return TYPES.getOrDefault(id, WaystoneTypes.STONE_TYPE);
    }

    public static Identifier getId(WaystoneType type) {
        return TYPES.inverse().get(type);
    }

    public static Identifier getIdOrDefault(WaystoneType type) {
        return TYPES.inverse().getOrDefault(type, STONE);
    }

    public static List<Identifier> getTypeIds() {
        return Collections.unmodifiableList(ALL_TYPE_IDS);
    }

    public static List<WaystoneType> getTypes() {
        return ALL_TYPE_IDS.stream().map(WaystoneTypes::getType).toList();
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

    public static final Identifier STONE = FabricWaystones.id("stone");
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
