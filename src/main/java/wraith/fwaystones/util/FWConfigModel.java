package wraith.fwaystones.util;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import wraith.fwaystones.FabricWaystones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Modmenu(modId = FabricWaystones.MOD_ID)
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
@Config(name = "fwaystones", wrapperName = "FWConfig")
public final class FWConfigModel {

    // waystone_block_hardness
    public float waystoneBlockHardness = 4F;                                                    // Controls the hardness for the waystone blocks
    // waystone_block_required_mining_level
    public Identifier waystoneBlockMiningTag = Identifier.of("minecraft:needs_stone_tool"); // Controls what mining level is required for the waystone blocks

    public LoggingLevel generalLoggingLevel = LoggingLevel.ALL;

    @SectionHeader(value = "interactions")
    //discover_with_item
    public String requiredDiscoveryItem = "";                           // Item required to discover waystones
    //take_amount_from_discover_item
    public int requiredDiscoveryAmount = 0;                             // Amount of a given item required to discover a waystone

    //consume_infinite_knowledge_scroll_on_use
    public boolean shouldConsumeInfiniteKnowledgeScroll = false;        // If the scroll of Infinite Knowledge will last forever

    public boolean allowSavingWaystoneData = true;
    //can_owners_redeem_payments
    public boolean allowOwnersToRedeemPayments = false;                 // Allows for the block to drop the payments saved within it like a Vending Machine

    @SectionHeader(value = "permissions")
    //permission_level_for_breaking_waystones
    public PermissionLevel breakingWaystonePermission = PermissionLevel.ANYONE; // Required permission level for breaking any waystones
    //global_mode_toggle_permission_levels
    public PermissionLevel globalTogglePermission = PermissionLevel.OWNER;      // Required permission level for toggling global mode on waystones

    @SectionHeader(value = "teleportation")
    //teleportation_cost
    @Nest
    public CostSettings teleportCost = new CostSettings();
    //teleportation_cooldown
    @Nest
    public CooldownSettings teleportCooldowns = new CooldownSettings();
    //consume_local_void_on_use
    public boolean shouldConsumeLocalVoid = true;                               // If the local void will be consumed on use for a teleport
    //free_local_void_teleport
    public boolean shouldLocalVoidTeleportBeFree = true;                        // A local void teleport dose not incur costs
    //disable_teleportation_from_dimensions
    public List<String> blacklistTeleportFromDimensions = new ArrayList<>();    // Disable the ability to teleport from the given dimensions
    //disable_teleportation_to_dimensions
    public List<String> blacklistTeleportToDimensions = new ArrayList<>();      // Disable the ability to teleport to the given dimensions
    //ignore_dimension_blacklists_if_same_dimension
    public boolean ignoreBlacklistForInterdimensionTravel = true;               // If the blacklist should prevent interdimensional travel

    public enum LoggingLevel {
        ALL,
        ERRORS,
        NONE
    }

    @SectionHeader(value = "world_generation")
    //unbreakable_generated_waystones
    public boolean unbreakableGeneratedWaystones = false;
    //generate_in_villages
    public boolean generateInVillages = true;
    //min_per_village
    public int minPerVillage = 1;
    //max_per_village
    public int maxPerVillage = 1;
    //village_waystone_weight
    public int villageWaystoneWeight = 2;
    //add_waystone_structure_piece
    public Map<String, String> structureAnchorToWaystoneAddition = Util.make(new HashMap<>(), map -> {
        map.put("minecraft:village/desert/houses", "desert_village_waystone");
        map.put("minecraft:village/plains/houses", "village_waystone");
        map.put("minecraft:village/savanna/houses", "village_waystone");
        map.put("minecraft:village/snowy/houses", "village_waystone");
        map.put("minecraft:village/taiga/houses", "village_waystone");

        map.put("ctov:village/beach/house", "desert_village_waystone");
        map.put("ctov:village/christmas/house", "village_waystone");
        map.put("ctov:village/desert/house", "desert_village_waystone");
        map.put("ctov:village/desert_oasis/house", "desert_village_waystone");
        map.put("ctov:village/halloween/house", "village_waystone");
        map.put("ctov:village/jungle/house", "mossy_stone_brick_village_waystone");
        map.put("ctov:village/jungle_tree/house", "mossy_stone_brick_village_waystone");
        map.put("ctov:village/mesa/house", "red_desert_village_waystone");
        map.put("ctov:village/mesa_fortified/house", "red_desert_village_waystone");
        map.put("ctov:village/mountain/house", "village_waystone");
        map.put("ctov:village/mountain_alpine/house", "village_waystone");
        map.put("ctov:village/mushroom/house", "village_waystone");
        map.put("ctov:village/plains/house", "village_waystone");
        map.put("ctov:village/plains_fortified/house", "village_waystone");
        map.put("ctov:village/savanna/house", "village_waystone");
        map.put("ctov:village/savanna_na/house", "village_waystone");
        map.put("ctov:village/snowy_igloo/house", "village_waystone");
        map.put("ctov:village/swamp/house", "village_waystone");
        map.put("ctov:village/swamp_fortified/house", "village_waystone");
        map.put("ctov:village/taiga/house", "village_waystone");
        map.put("ctov:village/taiga_fortified/house", "village_waystone");
        map.put("ctov:village/underground/house", "stone_brick_village_waystone");

        map.put("kaisyn:village/badlands_pueblo/houses", "village_waystone");
        map.put("kaisyn:village/beach_lighthouse/side", "village_waystone");
        map.put("kaisyn:village/birch_forest_romanian/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/classic/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/iberian/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/mediterranean/houses/regular", "village_waystone");
        map.put("kaisyn:village/exclusives/nilotic/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/rustic/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/swedish/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/tudor/houses", "village_waystone");
        map.put("kaisyn:village/exclusives/wandering_trader_camp/side", "village_waystone");
        map.put("kaisyn:village/flower_forest_japanese/houses", "village_waystone");
        map.put("kaisyn:village/grove_villager_outpost/decor", "village_waystone");
        map.put("kaisyn:village/jungle_tribal/houses", "village_waystone");
    });

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
        //cost_type
        public CostType type = CostType.LEVEL;
        //cost_item
        public Identifier item = Identifier.of("minecraft:ender_pearl");
        //base_cost
        public int baseAmount = 1;
        //cost_per_block_distance
        public float perBlockMultiplier = 0F;
        //cost_multiplier_between_dimensions
        public float dimensionMultiplier = 1F;
        public boolean allowBothMultipliers = true;
    }

    public static class CooldownSettings {
        //cooldown_ticks_when_hurt
        public int afterDamage = 0;
        //cooldown_ticks_from_abyss_watcher
        public int usedAbyssWatcher = 0;
        //cooldown_ticks_from_pocket_wormhole
        public int usedPockedWormhole = 0;
        //cooldown_ticks_from_local_void
        public int usedLocalVoid = 0;
        //cooldown_ticks_from_void_totem
        public int usedVoidTotem = 0;
        //cooldown_ticks_from_waystone
        public int usedWaystone = 0;
    }
}
