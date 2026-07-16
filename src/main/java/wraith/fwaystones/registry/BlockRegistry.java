package wraith.fwaystones.registry;

import io.wispforest.owo.util.TagInjector;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.util.Utils;
import java.util.HashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BlockRegistry {

    public static final Block BLACKSTONE_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "blackstone_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block DEEPSLATE_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "deepslate_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block DESERT_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "desert_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block ENDSTONE_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "end_stone_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block NETHER_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "nether_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block RED_DESERT_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "red_desert_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block RED_NETHER_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "red_nether_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block STONE_BRICK_WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "stone_brick_waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final Block WAYSTONE = new WaystoneBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("fwaystones", "waystone"))).mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(FabricWaystones.CONFIG.waystone_block_hardness(), 3600000));
    public static final HashMap<String, Block> WAYSTONE_BLOCKS = new HashMap<>();
    private static ResourceLocation miningLevelTag;

    public static void registerBlocks() {
        var miningLevel = FabricWaystones.CONFIG.waystone_block_required_mining_level();
        miningLevelTag = ResourceLocation.parse(switch (miningLevel) {
            case 1 -> "minecraft:needs_stone_tool";
            case 2 -> "minecraft:needs_iron_tool";
            case 3 -> "minecraft:needs_diamond_tool";
            default -> "fabric:needs_tool_level_" + miningLevel;
        });

        registerAndAdd("waystone", WAYSTONE);
        registerAndAdd("desert_waystone", DESERT_WAYSTONE);
        registerAndAdd("stone_brick_waystone", STONE_BRICK_WAYSTONE);
        registerAndAdd("red_desert_waystone", RED_DESERT_WAYSTONE);
        registerAndAdd("nether_brick_waystone", NETHER_BRICK_WAYSTONE);
        registerAndAdd("red_nether_brick_waystone", RED_NETHER_BRICK_WAYSTONE);
        registerAndAdd("end_stone_brick_waystone", ENDSTONE_BRICK_WAYSTONE);
        registerAndAdd("deepslate_brick_waystone", DEEPSLATE_BRICK_WAYSTONE);
        registerAndAdd("blackstone_brick_waystone", BLACKSTONE_BRICK_WAYSTONE);
    }

    private static void registerAndAdd(String id, Block block) {
        WAYSTONE_BLOCKS.put(id, block);
        Registry.register(BuiltInRegistries.BLOCK, Utils.ID(id), block);
        TagInjector.inject(BuiltInRegistries.BLOCK, miningLevelTag, block);
    }

}
