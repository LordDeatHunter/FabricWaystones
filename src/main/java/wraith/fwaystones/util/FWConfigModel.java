package wraith.fwaystones.util;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.Sync;
import wraith.fwaystones.FabricWaystones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Modmenu(modId = FabricWaystones.MOD_ID)
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "fwaystones/config", wrapperName = "FWConfig")
public final class FWConfigModel {

    @Nest
    public WorldgenSettings worldgen = new WorldgenSettings();
    @Nest
    public CostSettings teleportation_cost = new CostSettings();
    public String discover_with_item = "none";
    public int take_amount_from_discover_item = 0;
    public boolean consume_infinite_knowledge_scroll_on_use = false;
    public boolean consume_local_void_on_use = true;
    public boolean free_local_void_teleport = true;
    public boolean store_waystone_data_on_sneak_break = true;
    public boolean discover_waystone_on_map_use = false;
    public boolean can_owners_redeem_payments = false;
    public float waystone_block_hardness = 4F;
    public int waystone_block_required_mining_level = 1;
    public PermissionLevel permission_level_for_breaking_waystones = PermissionLevel.ANYONE;
    public PermissionLevel global_mode_toggle_permission_levels = PermissionLevel.OWNER;
    @Nest
    public CooldownSettings teleportation_cooldown = new CooldownSettings();
    public List<String> disable_teleportation_from_dimensions = new ArrayList<>();
    public List<String> disable_teleportation_to_dimensions = new ArrayList<>();
    public boolean ignore_dimension_blacklists_if_same_dimension = true;
    public Map<String, String> add_waystone_structure_piece = new HashMap<>() {{
        put("minecraft:village/desert/houses", "desert_village_waystone");
        put("minecraft:village/plains/houses", "village_waystone");
        put("minecraft:village/savanna/houses", "village_waystone");
        put("minecraft:village/snowy/houses", "village_waystone");
        put("minecraft:village/taiga/houses", "village_waystone");

        put("ctov:village/beach/house", "desert_village_waystone");
        put("ctov:village/christmas/house", "village_waystone");
        put("ctov:village/desert/house", "desert_village_waystone");
        put("ctov:village/desert_oasis/house", "desert_village_waystone");
        put("ctov:village/halloween/house", "village_waystone");
        put("ctov:village/jungle/house", "mossy_stone_brick_village_waystone");
        put("ctov:village/jungle_tree/house", "mossy_stone_brick_village_waystone");
        put("ctov:village/mesa/house", "red_desert_village_waystone");
        put("ctov:village/mesa_fortified/house", "red_desert_village_waystone");
        put("ctov:village/mountain/house", "village_waystone");
        put("ctov:village/mountain_alpine/house", "village_waystone");
        put("ctov:village/mushroom/house", "village_waystone");
        put("ctov:village/plains/house", "village_waystone");
        put("ctov:village/plains_fortified/house", "village_waystone");
        put("ctov:village/savanna/house", "village_waystone");
        put("ctov:village/savanna_na/house", "village_waystone");
        put("ctov:village/snowy_igloo/house", "village_waystone");
        put("ctov:village/swamp/house", "village_waystone");
        put("ctov:village/swamp_fortified/house", "village_waystone");
        put("ctov:village/taiga/house", "village_waystone");
        put("ctov:village/taiga_fortified/house", "village_waystone");
        put("ctov:village/underground/house", "stone_brick_village_waystone");

        put("kaisyn:village/badlands_pueblo/houses", "village_waystone");
        put("kaisyn:village/beach_lighthouse/side", "village_waystone");
        put("kaisyn:village/birch_forest_romanian/houses", "village_waystone");
        put("kaisyn:village/exclusives/classic/houses", "village_waystone");
        put("kaisyn:village/exclusives/iberian/houses", "village_waystone");
        put("kaisyn:village/exclusives/mediterranean/houses/regular", "village_waystone");
        put("kaisyn:village/exclusives/nilotic/houses", "village_waystone");
        put("kaisyn:village/exclusives/rustic/houses", "village_waystone");
        put("kaisyn:village/exclusives/swedish/houses", "village_waystone");
        put("kaisyn:village/exclusives/tudor/houses", "village_waystone");
        put("kaisyn:village/exclusives/wandering_trader_camp/side", "village_waystone");
        put("kaisyn:village/flower_forest_japanese/houses", "village_waystone");
        put("kaisyn:village/grove_villager_outpost/decor", "village_waystone");
        put("kaisyn:village/jungle_tribal/houses", "village_waystone");
    }};

    public enum PermissionLevel {
        OWNER,
        OP,
        ANYONE,
        NONE
    }

    public enum CostType {
        HEALTH,
        HUNGER,
        LEVEL,
        EXPERIENCE,
        ITEM,
        NONE
    }

    public static class CostSettings {

        public CostType cost_type = CostType.LEVEL;
        public String cost_item = "minecraft:ender_pearl";
        public int base_cost = 1;
        public float cost_per_block_distance = 0F;
        public float cost_multiplier_between_dimensions = 1F;
    }

    public static class CooldownSettings {

        public int cooldown_ticks_when_hurt = 0;
        public int cooldown_ticks_from_abyss_watcher = 0;
        public int cooldown_ticks_from_pocket_wormhole = 0;
        public int cooldown_ticks_from_local_void = 0;
        public int cooldown_ticks_from_void_totem = 0;
        public int cooldown_ticks_from_waystone = 0;

    }

    public static class WorldgenSettings {

        public boolean generate_in_villages = true;
        public boolean unbreakable_generated_waystones = false;
        public int min_per_village = 1;
        public int max_per_village = 1;
        public int village_waystone_weight = 2;
    }
}
