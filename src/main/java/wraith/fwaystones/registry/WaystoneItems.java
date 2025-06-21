package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneType;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.item.*;
import wraith.fwaystones.item.components.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WaystoneItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static final ItemGroup WAYSTONE_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        FabricWaystones.id(FabricWaystones.MOD_ID),
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(WaystoneBlocks.WAYSTONE))
            .displayName(Text.translatable("itemGroup.fwaystones.fwaystones"))
            .entries((enabledFeatures, entries) -> {
                ITEMS.values().stream().map(ItemStack::new).forEach(entries::add);

                var item = WaystoneBlocks.WAYSTONE.asItem();

                for (var typeId : WaystoneTypes.getTypeIds()) {
                    var stack = item.getDefaultStack();

                    stack.set(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(typeId));
                    entries.add(stack);
                }
            })
            .build()
    );

    public static final Item WAYSTONE_SCROLL = register(
        "waystone_scroll",
        new Item.Settings()
            .maxCount(1)
            .component(WaystoneDataComponents.HASH_TARGETS, WaystoneHashTargets.EMPTY)
    );
    public static final Item INFINITE_KNOWLEDGE_SCROLL = register(
        "infinite_knowledge_scroll",
        new Item.Settings()
            .component(WaystoneDataComponents.HAS_INFINITE_KNOWLEDGE, new InfiniteKnowledge())
            .maxCount(1)
            .fireproof()
    );
    public static final Item POCKET_WORMHOLE = register(
        "pocket_wormhole",
        new Item.Settings()
            .component(WaystoneDataComponents.TELEPORTER, new WaystoneTeleporter(false))
            .maxCount(1)
            .fireproof()
    );
    public static final Item ABYSS_WATCHER = register(
        "abyss_watcher",
        new Item(
            new Item.Settings()
                .component(WaystoneDataComponents.TELEPORTER, new WaystoneTeleporter(true))
                .maxCount(4)
                .fireproof()) {
            @Override
            public SoundEvent getBreakSound() {
                return SoundEvents.BLOCK_GLASS_BREAK;
            }
        }
    );

    public static final Item LOCAL_VOID = register(
        "local_void",
        new Item.Settings()
            .maxCount(1)
    );

    public static final Item VOID_TOTEM = register(
        "void_totem",
        new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.UNCOMMON)
    );

    public static final Item WAYSTONE_DEBUGGER = register(
        "waystone_debugger",
        new WaystoneDebuggerItem(
            new Item.Settings()
                .maxCount(1)
                .fireproof()
                .rarity(Rarity.EPIC))
    );

    public static final Item WAYSTONE = Registry.register(
        Registries.ITEM,
        FabricWaystones.id("waystone"),
        new BlockItem(
            WaystoneBlocks.WAYSTONE,
            new Item.Settings()
                .component(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(WaystoneTypes.STONE))
        ) {
            @Override
            public String getTranslationKey(ItemStack stack) {
                var data = stack.get(WaystoneDataComponents.WAYSTONE_TYPE);
                if (data != null) return data.getType().getTranslationKey();
                return "waystone.blank.name";
            }
        }
    );

    public static final Item WAYSTONE_COMPASS = register(
        "waystone_compass",
        new WaystoneCompassItem(
            new Item.Settings()
                .maxCount(1)
        )
    );

    private WaystoneItems() {}

    public static void init() {}

    private static Item register(String id, Item.Settings settings) {
        return register(id, new Item(settings));
    }

    private static Item register(String id, Item item) {
        var returned = Registry.register(Registries.ITEM, FabricWaystones.id(id), item);
        ITEMS.put(id, returned);
        return returned;
    }
}
