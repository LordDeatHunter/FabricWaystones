package wraith.fwaystones.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Scanner;

import static wraith.fwaystones.FabricWaystones.LOGGER;

// TODO: rewrite/migrate to new config system
public final class Config {

    private static final String CONFIG_FILE = "config/fwaystones/config.json";
    private static Config instance = null;
    public NbtCompound configData;
    private int difference = 0;

    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public static String readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\Z");
        var result = scanner.next();
        scanner.close();
        return result;
    }

    public static JsonObject getJsonObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public boolean generateInVillages() {
        return configData.getCompound("worldgen").getBoolean("generate_in_villages");
    }

    public int getMinPerVillage() {
        return configData.getCompound("worldgen").getInt("min_per_village");
    }

    public int getMaxPerVillage() {
        return configData.getCompound("worldgen").getInt("max_per_village");
    }

    public int getVillageStructureWeight() {
        return configData.getCompound("worldgen").getInt("village_waystone_weight");
    }

    public NbtCompound getWorldgenStructures() {
        return configData.getCompound("add_waystone_structure_piece");
    }

    public int getDiscoverItemAmount() {
        return Math.max(0, configData.getInt("take_amount_from_discover_item"));
    }

    public Identifier getDiscoverItem() {
        var discoverStr = configData.getString("discover_with_item");
        if (discoverStr.equals("none")) {
            return null;
        }
        String[] item = discoverStr.split(":");
        return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
    }

    public boolean canOwnersRedeemPayments() {
        return configData.getBoolean("can_owners_redeem_payments");
    }

    public boolean canPlayersToggleGlobal() {
        return configData.getBoolean("can_players_toggle_global_mode");
    }

    @Nullable
    public Identifier teleportCostItem() {
        if ("item".equals(configData.getCompound("teleportation_cost").getString("cost_type"))) {
            String[] item = configData.getCompound("teleportation_cost").getString("cost_item").split(":");
            return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
        }
        return null;
    }

    public boolean storeWaystoneNbt() {
        return configData.getBoolean("store_waystone_data_on_sneak_break");
    }

    public String teleportType() {
        return configData.getCompound("teleportation_cost").getString("cost_type");
    }

    public int baseTeleportCost() {
        return Math.abs(configData.getCompound("teleportation_cost").getInt("base_cost"));
    }

    public float perDimensionMultiplier() {
        return configData.getCompound("teleportation_cost").getFloat("cost_multiplier_between_dimensions");
    }

    public float extraCostPerBlock() {
        return Math.abs(configData.getCompound("teleportation_cost").getFloat("cost_per_block_distance"));
    }

    public float getHardness() {
        return configData.getFloat("waystone_block_hardness");
    }

    public int getMiningLevel() {
        return configData.getInt("waystone_block_required_mining_level");
    }

    public boolean consumeInfiniteScroll() {
        return configData.getBoolean("consume_infinite_knowledge_scroll_on_use");
    }

    public boolean consumeLocalVoid() {
        return configData.getBoolean("consume_local_void_on_use");
    }

    public boolean doLocalVoidsUseCost() {
        return !configData.getBoolean("free_local_void_teleport");
    }

    public boolean preventNonOwnersFromBreaking() {
        return configData.getBoolean("prevent_non_owners_from_breaking_waystone");
    }

    public int getCooldownWhenHurt() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_when_hurt");
    }

    public int getCooldownFromAbyssWatcher() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_from_abyss_watcher");
    }

    public int getCooldownFromPocketWormhole() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_from_pocket_wormhole");
    }

    public int getCooldownFromLocalVoid() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_from_local_void");
    }

    public int getCooldownFromVoidTotem() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_from_void_totem");
    }

    public int getCooldownFromWaystone() {
        return configData.getCompound("teleportation_cooldown").getInt("cooldown_ticks_from_waystone");
    }

    public int getIntOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getInt(key);
        } else {
            ++difference;
            return defaults.getInt(key);
        }
    }

    public boolean getBooleanOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getBoolean(key);
        } else {
            ++difference;
            return defaults.getBoolean(key);
        }
    }

    private String getStringOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getString(key);
        } else {
            ++difference;
            return defaults.getString(key);
        }
    }

    private NbtCompound getCompoundOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getCompound(key);
        } else {
            ++difference;
            return defaults.getCompound(key);
        }
    }

    private float getFloatOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getFloat(key);
        } else {
            ++difference;
            return defaults.getFloat(key);
        }
    }

    public int getIntOrDefault(JsonObject getFrom, String key, NbtCompound defaults) {
        if (getFrom.has(key)) {
            return getFrom.get(key).getAsInt();
        } else {
            ++difference;
            return defaults.getInt(key);
        }
    }

    public boolean getBooleanOrDefault(JsonObject getFrom, String key, NbtCompound defaults) {
        if (getFrom.has(key)) {
            return getFrom.get(key).getAsBoolean();
        } else {
            ++difference;
            return defaults.getBoolean(key);
        }
    }

    private String getStringOrDefault(JsonObject getFrom, String key, NbtCompound defaults) {
        if (getFrom.has(key)) {
            return getFrom.get(key).getAsString();
        } else {
            ++difference;
            return defaults.getString(key);
        }
    }

    private float getFloatOrDefault(JsonObject getFrom, String key, NbtCompound defaults) {
        if (getFrom.has(key)) {
            return getFrom.get(key).getAsFloat();
        } else {
            ++difference;
            return defaults.getFloat(key);
        }
    }

    private NbtCompound getDefaults() {
        NbtCompound defaultConfig = new NbtCompound();

        NbtCompound worldgen = new NbtCompound();
        worldgen.putBoolean("generate_in_villages", true);
        worldgen.putInt("min_per_village", 1);
        worldgen.putInt("max_per_village", 1);
        worldgen.putInt("village_waystone_weight", 2);
        defaultConfig.put("worldgen", worldgen);

        defaultConfig.putBoolean("consume_infinite_knowledge_scroll_on_use", false);
        defaultConfig.putBoolean("consume_local_void_on_use", true);
        defaultConfig.putBoolean("free_local_void_teleport", true);
        defaultConfig.putBoolean("can_owners_redeem_payments", false);
        defaultConfig.putBoolean("store_waystone_data_on_sneak_break", true);

        NbtCompound cost = new NbtCompound();
        cost.putString("cost_type", "level");
        cost.putString("cost_item", "minecraft:ender_pearl");
        cost.putInt("base_cost", 1);
        cost.putFloat("cost_per_block_distance", 0F);
        cost.putFloat("cost_multiplier_between_dimensions", 1F);
        defaultConfig.put("teleportation_cost", cost);

        defaultConfig.putFloat("waystone_block_hardness", 4F);
        defaultConfig.putInt("waystone_block_required_mining_level", 1);
        defaultConfig.putBoolean("prevent_non_owners_from_breaking_waystone", false);
        defaultConfig.putBoolean("can_players_toggle_global_mode", true);

        defaultConfig.putString("discover_with_item", "none");
        defaultConfig.putInt("take_amount_from_discover_item", 0);

        NbtCompound cooldown = new NbtCompound();
        cooldown.putInt("cooldown_ticks_when_hurt", 0);
        cooldown.putInt("cooldown_ticks_from_abyss_watcher", 0);
        cooldown.putInt("cooldown_ticks_from_pocket_wormhole", 0);
        cooldown.putInt("cooldown_ticks_from_local_void", 0);
        cooldown.putInt("cooldown_ticks_from_void_totem", 0);
        cooldown.putInt("cooldown_ticks_from_waystone", 0);
        defaultConfig.put("teleportation_cooldown", cooldown);

        NbtCompound waystoneStructures = new NbtCompound();
        waystoneStructures.putString("minecraft:village/plains/houses", "village_waystone");
        waystoneStructures.putString("minecraft:village/desert/houses", "desert_village_waystone");
        waystoneStructures.putString("minecraft:village/savanna/houses", "village_waystone");
        waystoneStructures.putString("minecraft:village/taiga/houses", "village_waystone");
        waystoneStructures.putString("minecraft:village/snowy/houses", "village_waystone");
        defaultConfig.put("add_waystone_structure_piece", waystoneStructures);

        return defaultConfig;
    }

    private JsonObject toJson(NbtCompound tag) {
        JsonObject json = new JsonObject();

        NbtCompound defaults = getDefaults();

        JsonObject worldgenJson = new JsonObject();
        NbtCompound worldgenTag = getCompoundOrDefault(tag, "worldgen", defaults);
        worldgenJson.addProperty("generate_in_villages", getBooleanOrDefault(worldgenTag, "generate_in_villages", defaults));
        worldgenJson.addProperty("min_per_village", getIntOrDefault(worldgenTag, "min_per_village", defaults));
        worldgenJson.addProperty("max_per_village", getIntOrDefault(worldgenTag, "max_per_village", defaults));
        worldgenJson.addProperty("village_waystone_weight", getIntOrDefault(worldgenTag, "village_waystone_weight", defaults));
        json.add("worldgen", worldgenJson);

        JsonObject costJson = new JsonObject();
        NbtCompound costTag = getCompoundOrDefault(tag, "teleportation_cost", defaults);
        costJson.addProperty("cost_type", getStringOrDefault(costTag, "cost_type", defaults));
        costJson.addProperty("cost_item", getStringOrDefault(costTag, "cost_item", defaults));
        costJson.addProperty("base_cost", getIntOrDefault(costTag, "base_cost", defaults));
        costJson.addProperty("cost_per_block_distance", getFloatOrDefault(costTag, "cost_per_block_distance", defaults));
        costJson.addProperty("cost_multiplier_between_dimensions", getFloatOrDefault(costTag, "cost_multiplier_between_dimensions", defaults));
        json.add("teleportation_cost", costJson);

        json.addProperty("discover_with_item", getStringOrDefault(tag, "discover_with_item", defaults));
        json.addProperty("take_amount_from_discover_item", getIntOrDefault(tag, "take_amount_from_discover_item", defaults));
        json.addProperty("consume_infinite_knowledge_scroll_on_use", getBooleanOrDefault(tag, "consume_infinite_knowledge_scroll_on_use", defaults));
        json.addProperty("consume_local_void_on_use", getBooleanOrDefault(tag, "consume_local_void_on_use", defaults));
        json.addProperty("free_local_void_teleport", getBooleanOrDefault(tag, "free_local_void_teleport", defaults));
        json.addProperty("store_waystone_data_on_sneak_break", getBooleanOrDefault(tag, "store_waystone_data_on_sneak_break", defaults));
        json.addProperty("can_owners_redeem_payments", getBooleanOrDefault(tag, "can_owners_redeem_payments", defaults));
        json.addProperty("waystone_block_hardness", getFloatOrDefault(tag, "waystone_block_hardness", defaults));
        json.addProperty("waystone_block_required_mining_level", getIntOrDefault(tag, "waystone_block_required_mining_level", defaults));
        json.addProperty("prevent_non_owners_from_breaking_waystone", getBooleanOrDefault(tag, "prevent_non_owners_from_breaking_waystone", defaults));
        json.addProperty("can_players_toggle_global_mode", getBooleanOrDefault(tag, "can_players_toggle_global_mode", defaults));

        JsonObject cooldownsJson = new JsonObject();
        NbtCompound cooldownsTag = getCompoundOrDefault(tag, "teleportation_cooldown", defaults);
        cooldownsJson.addProperty("cooldown_ticks_when_hurt", getIntOrDefault(cooldownsTag, "cooldown_ticks_when_hurt", defaults));
        cooldownsJson.addProperty("cooldown_ticks_from_abyss_watcher", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_abyss_watcher", defaults));
        cooldownsJson.addProperty("cooldown_ticks_from_pocket_wormhole", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_pocket_wormhole", defaults));
        cooldownsJson.addProperty("cooldown_ticks_from_local_void", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_local_void", defaults));
        cooldownsJson.addProperty("cooldown_ticks_from_void_totem", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_void_totem", defaults));
        cooldownsJson.addProperty("cooldown_ticks_from_waystone", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_waystone", defaults));
        json.add("teleportation_cooldown", cooldownsJson);

        JsonObject structuresJson = new JsonObject();
        NbtCompound structuresTag = getCompoundOrDefault(tag, "add_waystone_structure_piece", defaults);
        structuresTag.getKeys().forEach(key -> structuresJson.addProperty(key, structuresTag.getString(key)));
        json.add("add_waystone_structure_piece", structuresJson);

        createFile(json, difference > 0);
        difference = 0;
        return json;
    }

    private NbtCompound toNbtCompound(JsonObject json) {
        NbtCompound tag = new NbtCompound();

        NbtCompound defaults = getDefaults();

        NbtCompound worldgen = new NbtCompound();
        if (json.has("worldgen")) {
            var worldgenJson = json.get("worldgen").getAsJsonObject();
            var defaultWorldgen = new NbtCompound();
            worldgen.putBoolean("generate_in_villages", getBooleanOrDefault(worldgenJson, "generate_in_villages", defaultWorldgen));
            worldgen.putInt("min_per_village", getIntOrDefault(worldgenJson, "min_per_village", defaultWorldgen));
            worldgen.putInt("max_per_village", getIntOrDefault(worldgenJson, "max_per_village", defaultWorldgen));
            worldgen.putInt("village_waystone_weight", getIntOrDefault(worldgenJson, "village_waystone_weight", defaultWorldgen));
        } else {
            ++difference;
            worldgen = defaults.getCompound("worldgen");
        }
        tag.put("worldgen", worldgen);

        NbtCompound cost = new NbtCompound();
        if (json.has("teleportation_cost")) {
            var costJson = json.get("teleportation_cost").getAsJsonObject();
            var defaultCost = new NbtCompound();
            cost.putString("cost_type", getStringOrDefault(costJson, "cost_type", defaultCost));
            cost.putString("cost_item", getStringOrDefault(costJson, "cost_item", defaultCost));
            cost.putInt("base_cost", getIntOrDefault(costJson, "base_cost", defaultCost));
            cost.putFloat("cost_per_block_distance", getFloatOrDefault(costJson, "cost_per_block_distance", defaultCost));
            cost.putFloat("cost_multiplier_between_dimensions", getFloatOrDefault(costJson, "cost_multiplier_between_dimensions", defaultCost));
        } else {
            ++difference;
            cost = defaults.getCompound("teleportation_cost");
        }
        tag.put("teleportation_cost", cost);

        tag.putString("discover_with_item", getStringOrDefault(json, "discover_with_item", defaults));
        tag.putInt("take_amount_from_discover_item", getIntOrDefault(json, "take_amount_from_discover_item", defaults));
        tag.putBoolean("consume_infinite_knowledge_scroll_on_use", getBooleanOrDefault(json, "consume_infinite_knowledge_scroll_on_use", defaults));
        tag.putBoolean("consume_local_void_on_use", getBooleanOrDefault(json, "consume_local_void_on_use", defaults));
        tag.putBoolean("free_local_void_teleport", getBooleanOrDefault(json, "free_local_void_teleport", defaults));
        tag.putBoolean("store_waystone_data_on_sneak_break", getBooleanOrDefault(json, "store_waystone_data_on_sneak_break", defaults));
        tag.putBoolean("can_owners_redeem_payments", getBooleanOrDefault(json, "can_owners_redeem_payments", defaults));
        tag.putFloat("waystone_block_hardness", getFloatOrDefault(json, "waystone_block_hardness", defaults));
        tag.putInt("waystone_block_required_mining_level", getIntOrDefault(json, "waystone_block_required_mining_level", defaults));
        tag.putBoolean("prevent_non_owners_from_breaking_waystone", getBooleanOrDefault(json, "prevent_non_owners_from_breaking_waystone", defaults));
        tag.putBoolean("can_players_toggle_global_mode", getBooleanOrDefault(json, "can_players_toggle_global_mode", defaults));

        NbtCompound cooldowns = new NbtCompound();
        if (json.has("teleportation_cooldown")) {
            var cooldownsJson = json.get("teleportation_cooldown").getAsJsonObject();
            var defaultCooldowns = defaults.getCompound("teleportation_cooldown");
            cooldowns.putInt("cooldown_ticks_when_hurt", getIntOrDefault(cooldownsJson, "cooldown_ticks_when_hurt", defaultCooldowns));
            cooldowns.putInt("cooldown_ticks_from_abyss_watcher", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_abyss_watcher", defaultCooldowns));
            cooldowns.putInt("cooldown_ticks_from_pocket_wormhole", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_pocket_wormhole", defaultCooldowns));
            cooldowns.putInt("cooldown_ticks_from_local_void", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_local_void", defaultCooldowns));
            cooldowns.putInt("cooldown_ticks_from_void_totem", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_void_totem", defaultCooldowns));
            cooldowns.putInt("cooldown_ticks_from_waystone", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_waystone", defaultCooldowns));
        } else {
            ++difference;
            cooldowns = defaults.getCompound("teleportation_cooldown");
        }
        tag.put("teleportation_cooldown", cooldowns);

        NbtCompound waystoneStructuresNbt = new NbtCompound();
        if (json.has("add_waystone_structure_piece")) {
            var waystoneStructures = json.get("add_waystone_structure_piece").getAsJsonObject();
            for (var entry : waystoneStructures.entrySet()) {
                waystoneStructuresNbt.putString(entry.getKey(), entry.getValue().getAsString());
            }
        } else {
            ++difference;
            waystoneStructuresNbt = defaults.getCompound("add_waystone_structure_piece");
        }
        tag.put("add_waystone_structure_piece", waystoneStructuresNbt);

        createFile(toJson(tag), difference > 0);
        difference = 0;
        return tag;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean loadConfig() {
        try {
            return loadConfig(getJsonObject(readFile(new File(CONFIG_FILE))));
        } catch (Exception e) {
            LOGGER.info("Found error with config. Using default config.");
            this.configData = getDefaults();
            createFile(toJson(this.configData), true);
            return false;
        }
    }

    private boolean loadConfig(JsonObject fileConfig) {
        try {
            this.configData = toNbtCompound(fileConfig);
            return true;
        } catch (Exception e) {
            LOGGER.info("Found error with config. Using default config.");
            this.configData = getDefaults();
            createFile(toJson(this.configData), true);
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean loadConfig(NbtCompound config) {
        try {
            this.configData = config;
            return true;
        } catch (Exception e) {
            LOGGER.info("Found error with config. Using default config.");
            this.configData = getDefaults();
            createFile(toJson(this.configData), true);
            return false;
        }
    }

    private void createFile(JsonObject contents, boolean overwrite) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        contents = JsonParser.parseString(gson.toJson(contents)).getAsJsonObject();

        File file = new File(Config.CONFIG_FILE);
        if (file.exists() && !overwrite) {
            return;
        }
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        file.setReadable(true);
        file.setWritable(true);
        if (contents == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            String json = gson.toJson(contents).replace("\n", "").replace("\r", "");
            writer.write(gson.toJson(JsonParser.parseString(json).getAsJsonObject()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NbtCompound toNbtCompound() {
        return configData;
    }

    public void print(ServerPlayerEntity player) {
        var q = new LinkedList<JsonObject>();
        q.add(toJson(configData));
        while (!q.isEmpty()) {
            var current = q.poll();
            for (var entry : current.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (value.isJsonObject()) {
                    q.add(value.getAsJsonObject());
                    continue;
                }
                player.sendMessage(Text.literal("§6[§e" + key + "§6] §3 " + value), false);
            }
        }
    }

}
