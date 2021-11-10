package wraith.waystones.util;

import com.google.gson.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
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
    
    public boolean preventNonOwnersFromBreaking() {
        return configData.getBoolean("prevent_non_owners_from_breaking_waystone");
    }

    private NbtCompound getDefaults() {
        NbtCompound defaultConfig = new NbtCompound();

        defaultConfig.putBoolean("generate_in_villages", true);
        defaultConfig.putBoolean("consume_infinite_knowledge_scroll_on_use", false);
        defaultConfig.putBoolean("can_owners_redeem_payments", false);
        defaultConfig.putBoolean("store_waystone_data_on_sneak_break", true);
        defaultConfig.putInt("cost_amount", 1);
        defaultConfig.putString("cost_type", "level");
        defaultConfig.putString("cost_item", "minecraft:ender_pearl");
        defaultConfig.putFloat("waystone_block_hardness", 4F);
        defaultConfig.putInt("waystone_block_required_mining_level", 1);
        defaultConfig.putBoolean("prevent_non_owners_from_breaking_waystone", false);

        return defaultConfig;
    }

    private JsonObject toJson(NbtCompound tag) {
        boolean overwrite = false;
        JsonObject json = new JsonObject();

        NbtCompound defaults = getDefaults();

        boolean generateInVillages;
        if (tag.contains("generate_in_villages")) {
            generateInVillages = tag.getBoolean("generate_in_villages");
        } else {
            overwrite = true;
            generateInVillages = defaults.getBoolean("generate_in_villages");
        }
        json.addProperty("generate_in_villages", generateInVillages);

        boolean consumeInfiniteScroll;
        if (tag.contains("consume_infinite_knowledge_scroll_on_use")) {
            consumeInfiniteScroll = tag.getBoolean("consume_infinite_knowledge_scroll_on_use");
        } else {
            overwrite = true;
            consumeInfiniteScroll = defaults.getBoolean("consume_infinite_knowledge_scroll_on_use");
        }
        json.addProperty("consume_infinite_knowledge_scroll_on_use", consumeInfiniteScroll);

        boolean storeWaystoneDataOnBreak;
        if (tag.contains("store_waystone_data_on_sneak_break")) {
            storeWaystoneDataOnBreak = tag.getBoolean("store_waystone_data_on_sneak_break");
        } else {
            overwrite = true;
            storeWaystoneDataOnBreak = defaults.getBoolean("store_waystone_data_on_sneak_break");
        }
        json.addProperty("store_waystone_data_on_sneak_break", storeWaystoneDataOnBreak);

        boolean canOnwersRedeem;
        if (tag.contains("can_owners_redeem_payments")) {
            canOnwersRedeem = tag.getBoolean("can_owners_redeem_payments");
        } else {
            overwrite = true;
            canOnwersRedeem = defaults.getBoolean("can_owners_redeem_payments");
        }
        json.addProperty("can_owners_redeem_payments", canOnwersRedeem);

        int costAmount;
        if (tag.contains("cost_amount")) {
            costAmount = tag.getInt("cost_amount");
        } else {
            overwrite = true;
            costAmount = defaults.getInt("cost_amount");
        }
        json.addProperty("cost_amount", costAmount);

        String costType;
        if (tag.contains("cost_type")) {
            costType = tag.getString("cost_type").toLowerCase();
        } else {
            overwrite = true;
            costType = defaults.getString("cost_type");
        }
        json.addProperty("cost_type", costType);

        String costItem;
        if (tag.contains("cost_item")) {
            costItem = tag.getString("cost_item").toLowerCase();
        } else {
            overwrite = true;
            costItem = defaults.getString("cost_item");
        }
        json.addProperty("cost_item", costItem);

        int blockHardness;
        if (tag.contains("waystone_block_hardness")) {
            blockHardness = tag.getInt("waystone_block_hardness");
        } else {
            overwrite = true;
            blockHardness = defaults.getInt("waystone_block_hardness");
        }
        json.addProperty("waystone_block_hardness", blockHardness);

        float miningLevel;
        if (tag.contains("waystone_block_required_mining_level")) {
            miningLevel = tag.getFloat("waystone_block_required_mining_level");
        } else {
            overwrite = true;
            miningLevel = defaults.getFloat("waystone_block_required_mining_level");
        }
        json.addProperty("waystone_block_required_mining_level", miningLevel);

        boolean preventNonOwnersBreaking;
        if (tag.contains("prevent_non_owners_from_breaking_waystone")) {
            preventNonOwnersBreaking = tag.getBoolean("prevent_non_owners_from_breaking_waystone");
        } else {
            overwrite = true;
            preventNonOwnersBreaking = defaults.getBoolean("prevent_non_owners_from_breaking_waystone");
        }
        json.addProperty("prevent_non_owners_from_breaking_waystone", preventNonOwnersBreaking);

        createFile(json, overwrite);
        return json;
    }

    private NbtCompound toNbtCompound(JsonObject json) {
        boolean overwrite = false;
        NbtCompound tag = new NbtCompound();

        NbtCompound defaults = getDefaults();

        boolean generateInVillages;
        if (json.has("generate_in_villages")) {
            generateInVillages = json.get("generate_in_villages").getAsBoolean();
        } else {
            overwrite = true;
            generateInVillages = defaults.getBoolean("generate_in_villages");
        }
        tag.putBoolean("generate_in_villages", generateInVillages);

        boolean consumeInfiniteScroll;
        if (json.has("consume_infinite_knowledge_scroll_on_use")) {
            consumeInfiniteScroll = json.get("consume_infinite_knowledge_scroll_on_use").getAsBoolean();
        } else {
            overwrite = true;
            consumeInfiniteScroll = defaults.getBoolean("consume_infinite_knowledge_scroll_on_use");
        }
        tag.putBoolean("consume_infinite_knowledge_scroll_on_use", consumeInfiniteScroll);

        boolean storeWaystoneDataOnBreak;
        if (json.has("store_waystone_data_on_sneak_break")) {
            storeWaystoneDataOnBreak = json.get("store_waystone_data_on_sneak_break").getAsBoolean();
        } else {
            overwrite = true;
            storeWaystoneDataOnBreak = defaults.getBoolean("store_waystone_data_on_sneak_break");
        }
        tag.putBoolean("store_waystone_data_on_sneak_break", storeWaystoneDataOnBreak);

        boolean canOnwersRedeem;
        if (json.has("can_owners_redeem_payments")) {
            canOnwersRedeem = json.get("can_owners_redeem_payments").getAsBoolean();
        } else {
            overwrite = true;
            canOnwersRedeem = defaults.getBoolean("can_owners_redeem_payments");
        }
        tag.putBoolean("can_owners_redeem_payments", canOnwersRedeem);

        int costAmount;
        if (json.has("cost_amount")) {
            costAmount = json.get("cost_amount").getAsInt();
        } else {
            overwrite = true;
            costAmount = defaults.getInt("cost_amount");
        }
        tag.putInt("cost_amount", costAmount);

        String costItem;
        if (json.has("cost_item")) {
            costItem = json.get("cost_item").getAsString();
        } else {
            overwrite = true;
            costItem = defaults.getString("cost_item");
        }
        tag.putString("cost_item", costItem);

        String costType;
        if (json.has("cost_type")) {
            costType = json.get("cost_type").getAsString();
        } else {
            overwrite = true;
            costType = defaults.getString("cost_type");
        }
        tag.putString("cost_type", costType);

        int blockHardness;
        if (json.has("waystone_block_hardness")) {
            blockHardness = json.get("waystone_block_hardness").getAsInt();
        } else {
            overwrite = true;
            blockHardness = defaults.getInt("waystone_block_hardness");
        }
        tag.putInt("waystone_block_hardness", blockHardness);

        float miningLevel;
        if (json.has("waystone_block_required_mining_level")) {
            miningLevel = json.get("waystone_block_required_mining_level").getAsFloat();
        } else {
            overwrite = true;
            miningLevel = defaults.getFloat("waystone_block_required_mining_level");
        }
        tag.putFloat("waystone_block_required_mining_level", miningLevel);

        boolean preventNonOwnersBreaking;
        if (json.has("prevent_non_owners_from_breaking_waystone")) {
            preventNonOwnersBreaking = json.get("prevent_non_owners_from_breaking_waystone").getAsBoolean();
        } else {
            overwrite = true;
            preventNonOwnersBreaking = defaults.getBoolean("prevent_non_owners_from_breaking_waystone");
        }
        tag.putBoolean("prevent_non_owners_from_breaking_waystone", preventNonOwnersBreaking);

        createFile(toJson(tag), overwrite);
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
