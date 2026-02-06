package wraith.fwaystones.registry;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.item.BindstoneItem;
import wraith.fwaystones.item.WaystoneBlockItem;
import wraith.fwaystones.item.WaystoneCompassItem;
import wraith.fwaystones.item.WaystoneDebuggerItem;
import wraith.fwaystones.item.components.InfiniteKnowledge;
import wraith.fwaystones.item.components.WaystoneHashTargets;
import wraith.fwaystones.item.components.WaystoneTeleporter;
import wraith.fwaystones.item.components.WaystoneTyped;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WaystoneItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static final OwoItemGroup WAYSTONES_GROUP = OwoItemGroup.builder(
            FabricWaystones.id(FabricWaystones.MOD_ID),
            () -> Icon.of(WaystoneItems.WAYSTONE.getDefaultStack())
        )
        .initializer(group -> {
            group.addCustomTab(
                Util.make(() -> {
                    var stack = WaystoneItems.WAYSTONE.getDefaultStack();
                    stack.remove(WaystoneDataComponents.WAYSTONE_TYPE);
                    return Icon.of(stack);
                }),
                "waystones",
                (context, entries) -> {
                    for (var typeId : WaystoneTypes.getTypeIds()) {
                        var stack = WaystoneItems.WAYSTONE.getDefaultStack();
                        stack.set(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(typeId));
                        entries.add(stack);
                    }
                    for (var typeId : WaystoneTypes.getTypeIds()) {
                        var stack = WaystoneItems.SMALL_WAYSTONE.getDefaultStack();
                        stack.set(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(typeId));
                        entries.add(stack);
                    }
                    for (var typeId : WaystoneTypes.getTypeIds()) {
                        var stack = WaystoneItems.WAYPLATE.getDefaultStack();
                        stack.set(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(typeId));
                        entries.add(stack);
                    }
                },
                true
            );
            group.addCustomTab(
                Icon.of(WaystoneItems.ABYSS_WATCHER.getDefaultStack()),
                "items",
                (context, entries) -> ITEMS.values().stream().map(ItemStack::new).forEach(entries::add),
                false
            );
            group.addButton(ItemGroupButton.modrinth(WaystoneItems.WAYSTONES_GROUP, "https://modrinth.com/mod/fwaystones"));
            group.addButton(ItemGroupButton.curseforge(WaystoneItems.WAYSTONES_GROUP, "https://www.curseforge.com/minecraft/mc-mods/fabric-waystones"));
            group.addButton(ItemGroupButton.github(WaystoneItems.WAYSTONES_GROUP, "https://github.com/LordDeatHunter/FabricWaystones"));
            group.addButton(ItemGroupButton.discord(WaystoneItems.WAYSTONES_GROUP, "https://discord.gg/zFHWx42VXU"));
        })
        .build();

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

    public static final Item POCKET_WORMHOLE = register(
        "pocket_wormhole",
        new Item.Settings()
            .component(WaystoneDataComponents.TELEPORTER, new WaystoneTeleporter(false))
            .maxCount(1)
            .fireproof()
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

    public static final Item WAYSTONE_COMPASS = register(
        "waystone_compass",
        new WaystoneCompassItem(
            new Item.Settings()
                .maxCount(1)
        )
    );

    public static final Item BINDSTONE = register(
        "bindstone",
        new BindstoneItem(
            new Item.Settings()
                .maxCount(1)
        )
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
        new WaystoneBlockItem(WaystoneBlocks.WAYSTONE, new Item.Settings())
    );

    public static final Item SMALL_WAYSTONE = Registry.register(
        Registries.ITEM,
        FabricWaystones.id("small_waystone"),
        new WaystoneBlockItem(WaystoneBlocks.WAYSTONE_SMALL, new Item.Settings())
    );

    public static final Item WAYPLATE = Registry.register(
        Registries.ITEM,
        FabricWaystones.id("wayplate"),
        new WaystoneBlockItem(WaystoneBlocks.WAYPLATE, new Item.Settings())
    );

    public static void init() {
        WAYSTONES_GROUP.initialize();
    }

    private static Item register(String id, Item.Settings settings) {
        return register(id, new Item(settings));
    }

    private static Item register(String id, Item item) {
        var returned = Registry.register(Registries.ITEM, FabricWaystones.id(id), item);
        ITEMS.put(id, returned);
        return returned;
    }
}
