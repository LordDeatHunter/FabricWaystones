package wraith.waystones.util;

import com.google.gson.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Waystones;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Map;
import java.util.Scanner;

public final class Config {
    private static Config instance = null;

    public NbtCompound configData;
    private final Logger LOGGER = Waystones.LOGGER;
    private static final String CONFIG_FILE = "config/waystones/config.json";
    private int difference = 0;

    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public boolean generateInVillages() {
        return configData.getBoolean("generate_in_villages");
    }

    public boolean canOwnersRedeemPayments() {
        return configData.getBoolean("can_owners_redeem_payments");
    }

    public boolean canPlayersToggleGlobal() {
        return configData.getBoolean("can_players_toggle_global_mode");
    }

    @Nullable
    public Identifier teleportCostItem() {
        if ("item".equals(configData.getString("cost_type"))) {
            String[] item = configData.getString("cost_item").split(":");
            return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
        }
        return null;
    }

    public boolean storeWaystoneNbt() {
        return true;
    }

    public String teleportType() {
        return configData.getString("cost_type");
    }

    public int teleportCost() {
        return Math.abs(configData.getInt("cost_amount"));
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

    private double getDoubleOrDefault(NbtCompound getFrom, String key, NbtCompound defaults) {
        if (getFrom.contains(key)) {
            return getFrom.getDouble(key);
        } else {
            ++difference;
            return defaults.getDouble(key);
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

    private double getDoubleOrDefault(JsonObject getFrom, String key, NbtCompound defaults) {
        if (getFrom.has(key)) {
            return getFrom.get(key).getAsDouble();
        } else {
            ++difference;
            return defaults.getDouble(key);
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

        defaultConfig.putBoolean("generate_in_villages", true);
        defaultConfig.putBoolean("consume_infinite_knowledge_scroll_on_use", false);
        defaultConfig.putBoolean("consume_local_void_on_use", true);
        defaultConfig.putBoolean("can_owners_redeem_payments", false);
        defaultConfig.putBoolean("store_waystone_data_on_sneak_break", true);
        defaultConfig.putInt("cost_amount", 1);
        defaultConfig.putString("cost_type", "level");
        defaultConfig.putString("cost_item", "minecraft:ender_pearl");
        defaultConfig.putFloat("waystone_block_hardness", 4F);
        defaultConfig.putInt("waystone_block_required_mining_level", 1);
        defaultConfig.putBoolean("prevent_non_owners_from_breaking_waystone", false);
        defaultConfig.putBoolean("can_players_toggle_global_mode", true);

        NbtCompound cooldown = new NbtCompound();
        cooldown.putInt("cooldown_ticks_when_hurt", 0);
        cooldown.putInt("cooldown_ticks_from_abyss_watcher", 0);
        cooldown.putInt("cooldown_ticks_from_pocket_wormhole", 0);
        cooldown.putInt("cooldown_ticks_from_local_void", 0);
        cooldown.putInt("cooldown_ticks_from_waystone", 0);
        defaultConfig.put("teleportation_cooldown", cooldown);

        return defaultConfig;
    }

    private JsonObject toJson(NbtCompound tag) {
        JsonObject json = new JsonObject();

        NbtCompound defaults = getDefaults();

        json.addProperty("generate_in_villages", getBooleanOrDefault(tag, "generate_in_villages", defaults));
        json.addProperty("consume_infinite_knowledge_scroll_on_use", getBooleanOrDefault(tag, "consume_infinite_knowledge_scroll_on_use", defaults));
        json.addProperty("consume_local_void_on_use", getBooleanOrDefault(tag, "consume_local_void_on_use", defaults));
        json.addProperty("store_waystone_data_on_sneak_break", getBooleanOrDefault(tag, "store_waystone_data_on_sneak_break", defaults));
        json.addProperty("can_owners_redeem_payments", getBooleanOrDefault(tag, "can_owners_redeem_payments", defaults));
        json.addProperty("cost_amount", getIntOrDefault(tag, "cost_amount", defaults));
        json.addProperty("cost_type", getStringOrDefault(tag, "cost_type", defaults));
        json.addProperty("cost_item", getStringOrDefault(tag, "cost_item", defaults));
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
        cooldownsJson.addProperty("cooldown_ticks_from_waystone", getIntOrDefault(cooldownsTag, "cooldown_ticks_from_waystone", defaults));
        json.add("teleportation_cooldown", cooldownsJson);

        createFile(json, difference > 0);
        difference = 0;
        return json;
    }

    private NbtCompound toNbtCompound(JsonObject json) {
        NbtCompound tag = new NbtCompound();

        NbtCompound defaults = getDefaults();

        tag.putBoolean("generate_in_villages", getBooleanOrDefault(json, "generate_in_villages", defaults));
        tag.putBoolean("consume_infinite_knowledge_scroll_on_use", getBooleanOrDefault(json, "consume_infinite_knowledge_scroll_on_use", defaults));
        tag.putBoolean("consume_local_void_on_use", getBooleanOrDefault(json, "consume_local_void_on_use", defaults));
        tag.putBoolean("store_waystone_data_on_sneak_break", getBooleanOrDefault(json, "store_waystone_data_on_sneak_break", defaults));
        tag.putBoolean("can_owners_redeem_payments", getBooleanOrDefault(json, "can_owners_redeem_payments", defaults));
        tag.putInt("cost_amount", getIntOrDefault(json, "cost_amount", defaults));
        tag.putString("cost_type", getStringOrDefault(json, "cost_type", defaults));
        tag.putString("cost_item", getStringOrDefault(json, "cost_item", defaults));
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
            cooldowns.putInt("cooldown_ticks_from_waystone", getIntOrDefault(cooldownsJson, "cooldown_ticks_from_waystone", defaultCooldowns));
        } else {
            ++difference;
            cooldowns = defaults.getCompound("teleportation_cooldown");
        }
        tag.put("teleportation_cooldown", cooldowns);

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
        contents = new JsonParser().parse(gson.toJson(contents)).getAsJsonObject();

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
        file.setExecutable(true);
        if (contents == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            String json = gson.toJson(contents).replace("\n", "").replace("\r", "");
            writer.write(gson.toJson(new JsonParser().parse(json).getAsJsonObject()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NbtCompound toNbtCompound() {
        return configData;
    }

    public static String readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\Z");
        var result = scanner.next();
        scanner.close();
        return result;
    }

    public static JsonObject getJsonObject(String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public void print(ServerPlayerEntity player) {
        for (Map.Entry<String, JsonElement> config : toJson(configData).entrySet()) {
            if (config.getValue().isJsonObject()) {
                continue;
            }
            player.sendMessage(new LiteralText("§6[§e" + config.getKey() + "§6] §3 " + config.getValue()), false);
        }
    }

}
