package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.*;
import wraith.fwaystones.item.components.InfiniteKnowledge;
import wraith.fwaystones.item.components.WaystoneHashTargets;
import wraith.fwaystones.item.components.WaystoneTeleporter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WaystoneItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    public static final ItemGroup WAYSTONE_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(WaystoneBlocks.WAYSTONE))
            .displayName(Text.translatable("itemGroup.fwaystones.fwaystones"))
            .entries((enabledFeatures, entries) -> ITEMS.values().stream().map(ItemStack::new).forEach(entries::add))
            .build();

    public static final Item WAYSTONE_COMPASS = Registry.register(Registries.ITEM, FabricWaystones.id("waystone_compass"), new WaystoneCompassItem(new Item.Settings().maxCount(1).fireproof()));

    private WaystoneItems() {}

    private static void registerItem(String id, Item item) {
        ITEMS.put(id, Registry.register(Registries.ITEM, FabricWaystones.id(id), item));
    }

    public static void init() {
        if (!ITEMS.isEmpty()) return;

        Registry.register(Registries.ITEM_GROUP, FabricWaystones.id(FabricWaystones.MOD_ID), WAYSTONE_GROUP);
        registerItem("waystone", new BlockItem(WaystoneBlocks.WAYSTONE, new Item.Settings()));
        registerItem("desert_waystone", new BlockItem(WaystoneBlocks.DESERT_WAYSTONE, new Item.Settings()));
        registerItem("red_desert_waystone", new BlockItem(WaystoneBlocks.RED_DESERT_WAYSTONE, new Item.Settings()));
        registerItem("stone_brick_waystone", new BlockItem(WaystoneBlocks.STONE_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("nether_brick_waystone", new BlockItem(WaystoneBlocks.NETHER_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("red_nether_brick_waystone", new BlockItem(WaystoneBlocks.RED_NETHER_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("end_stone_brick_waystone", new BlockItem(WaystoneBlocks.ENDSTONE_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("deepslate_brick_waystone", new BlockItem(WaystoneBlocks.DEEPSLATE_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("blackstone_brick_waystone", new BlockItem(WaystoneBlocks.BLACKSTONE_BRICK_WAYSTONE, new Item.Settings()));
        registerItem("waystone_scroll", new Item(new Item.Settings().maxCount(1).component(WaystoneDataComponents.HASH_TARGETS, WaystoneHashTargets.EMPTY)));
        registerItem("scroll_of_infinite_knowledge", new Item(new Item.Settings().component(WaystoneDataComponents.HAS_INFINITE_KNOWLEDGE, new InfiniteKnowledge()).maxCount(1).fireproof()));
        registerItem("pocket_wormhole", new Item(new Item.Settings().component(WaystoneDataComponents.TELEPORTER, new WaystoneTeleporter(false)).maxCount(1).fireproof()));
        registerItem("abyss_watcher", new Item(new Item.Settings().component(WaystoneDataComponents.TELEPORTER, new WaystoneTeleporter(true)).maxCount(4).fireproof()){
            @Override
            public SoundEvent getBreakSound() {
                return SoundEvents.BLOCK_GLASS_BREAK;
            }
        });
        registerItem("local_void", new Item(new Item.Settings().maxCount(1)));
        registerItem("void_totem", new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
        registerItem("waystone_debugger", new WaystoneDebuggerItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)));
    }

    public static Item get(String id) {
        return ITEMS.getOrDefault(id, Items.AIR);
    }

}
