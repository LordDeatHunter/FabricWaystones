package wraith.waystones;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Config {

    private static Config instance = null;
    private CompoundTag configData;
    private final Logger LOGGER = Waystones.LOGGER;
    private static final String CONFIG_FILE = "config/waystones/config.json";

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public boolean generateInVillages() {
        return configData.getBoolean("generate_in_villages");
    }

    public Identifier teleportCostItem() {
        if ("item".equals(configData.getString("cost_type"))) {
            String[] item = configData.getString("cost_item").split(":");
            return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
        }
        return null;
    }

    public String teleportType() {
        return configData.getString("cost_type");
    }

    public int teleportCost() {
        return Math.abs(configData.getInt("cost_amount"));
    }

    public boolean canGlobalDiscover() {
        return configData.getBoolean("global_discover");
    }

    public float getHardness() {
        return configData.getFloat("waystone_block_hardness");
    }

    public int getMiningLevel() {
        return configData.getInt("waystone_block_required_mining_level");
    }

    private Config() {
        createDefaults();
    }

    private CompoundTag getDefaults() {
        CompoundTag defaultConfig = new CompoundTag();

        defaultConfig.putBoolean("global_discover", false);
        defaultConfig.putBoolean("generate_in_villages", true);
        defaultConfig.putInt("cost_amount", 1);
        defaultConfig.putString("cost_type", "level");
        defaultConfig.putString("cost_item", "minecraft:ender_pearl");
        defaultConfig.putFloat("waystone_block_hardness", 4F);
        defaultConfig.putInt("waystone_block_required_mining_level", 1);

        CompoundTag recipesTag = new CompoundTag();

        HashMap<String, String> itemMap = new HashMap<>();
        itemMap.put("S", "minecraft:stone_bricks");
        itemMap.put("A", "waystones:abyss_watcher");
        itemMap.put("O", "minecraft:obsidian");
        itemMap.put("E", "minecraft:emerald");
        recipesTag.putString("waystone_recipe", Utils.createRecipe("SAS_SES_SOS", itemMap, "waystones:waystone", 1).toString());

        itemMap.clear();
        itemMap.put("A", "waystones:abyss_watcher");
        itemMap.put("S", "minecraft:nether_star");
        itemMap.put("P", "minecraft:blaze_powder");
        recipesTag.putString("pocket_wormhole", Utils.createRecipe(" A _PSP_ P ", itemMap, "waystones:pocket_wormhole", 1).toString());

        itemMap.clear();
        itemMap.put("E", "minecraft:ender_pearl");
        itemMap.put("F", "minecraft:flint");
        recipesTag.putString("abyss_watcher", Utils.createRecipe("FEF", itemMap, "waystones:abyss_watcher", 1).toString());

        itemMap.clear();
        itemMap.put("P", "minecraft:paper");
        itemMap.put("S", "minecraft:stick");
        recipesTag.putString("empty_scroll", Utils.createRecipe("SPS_PPP_SPS", itemMap, "waystones:empty_scroll", 1).toString());

        defaultConfig.put("recipes", recipesTag);

        return defaultConfig;
    }

    private void createDefaults() {
        createFile(toJson(getDefaults()), false);
    }

    private JsonObject toJson(CompoundTag tag) {
        JsonObject json = new JsonObject();

        CompoundTag defaults = getDefaults();

        boolean globalDiscover = tag.contains("global_discover") ? tag.getBoolean("global_discover") : defaults.getBoolean("global_discover");
        json.addProperty("global_discover", globalDiscover);

        boolean generateInVillages = tag.contains("generate_in_villages") ? tag.getBoolean("generate_in_villages") : defaults.getBoolean("generate_in_villages");
        json.addProperty("generate_in_villages", generateInVillages);

        int costAmount = tag.contains("cost_amount") ? tag.getInt("cost_amount") : defaults.getInt("cost_amount");
        json.addProperty("cost_amount", costAmount);

        String costItem = tag.contains("cost_item") ? tag.getString("cost_item") : defaults.getString("cost_item");
        json.addProperty("cost_item", costItem);

        int blockHardness = tag.contains("waystone_block_hardness") ? tag.getInt("waystone_block_hardness") : defaults.getInt("waystone_block_hardness");
        json.addProperty("waystone_block_hardness", blockHardness);

        float miningLevel = tag.contains("waystone_block_required_mining_level") ? tag.getFloat("waystone_block_required_mining_level") : defaults.getFloat("waystone_block_required_mining_level");
        json.addProperty("waystone_block_required_mining_level", miningLevel);

        JsonObject recipesJson = new JsonObject();
        CompoundTag recipesTag = tag.getCompound("recipes");
        for (String recipe : recipesTag.getKeys()) {
            recipesJson.addProperty(recipe, recipesTag.getString(recipe));
        }
        json.add("recipes", recipesJson);
        return json;
    }

    private CompoundTag toCompoundTag(JsonObject json) {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("global_discover", json.get("global_discover").getAsBoolean());
        tag.putBoolean("generate_in_villages", json.get("generate_in_villages").getAsBoolean());
        tag.putInt("cost_amount", json.get("cost_amount").getAsInt());
        tag.putString("cost_item", json.get("cost_item").getAsString());
        tag.putInt("waystone_block_hardness", json.get("waystone_block_hardness").getAsInt());
        tag.putFloat("waystone_block_required_mining_level", json.get("waystone_block_required_mining_level").getAsFloat());

        JsonObject recipesJson = json.get("recipes").getAsJsonObject();
        CompoundTag recipesTag = new CompoundTag();

        for (Map.Entry<String, JsonElement> recipe : recipesJson.entrySet()) {
            recipesTag.putString(recipe.getKey(), recipe.getValue().getAsString());
        }
        tag.put("recipes", recipesTag);
        return tag;
    }

    public boolean loadConfig() {
        try {
            JsonObject fileConfig = getJsonObject(readFile(new File(CONFIG_FILE)));
            return loadConfig(fileConfig);
        } catch (JsonParseException ex) {
            LOGGER.info("Found error with config. Using default config.");
            return false;
        }
    }

    private boolean loadConfig(JsonObject fileConfig) {
        try {
            CompoundTag config = toCompoundTag(fileConfig);
            this.configData = config;
            return true;
        } catch (Exception ex) {
            LOGGER.info("Found error with config. Using default config.");
            this.configData = getDefaults();
            return false;
        }
    }

    public boolean loadConfig(CompoundTag config) {
        try {
            this.configData = config;
            return true;
        } catch (Exception ex) {
            LOGGER.info("Found error with config. Using default config.");
            this.configData = getDefaults();
            return false;
        }
    }

    private void createFile(JsonObject contents, boolean overwrite) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringBuilder recipes = new StringBuilder();
        if (contents != null && contents.has("recipes")) {
            for (Map.Entry<String, JsonElement> recipe : contents.get("recipes").getAsJsonObject().entrySet()) {
                recipes.append("\"").append(recipe.getKey()).append("\": ").append(gson.toJson(new JsonParser().parse(recipe.getValue().getAsString()).getAsJsonObject())).append(",\n");
            }
            recipes = new StringBuilder(recipes.substring(0, recipes.length() - 2));
            recipes.append("\n}");
            contents.remove("recipes");
        }

        File file = new File(Config.CONFIG_FILE);
        if (file.exists() && !overwrite) {
            return;
        }
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.setReadable(true);
        file.setWritable(true);
        file.setExecutable(true);
        if (contents == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            String json = gson.toJson(contents);
            json = json.substring(0, json.length() - 2) + ",\n" + recipes;
            writer.write(gson.toJson(new JsonParser().parse(json).getAsJsonObject()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompoundTag toCompoundTag() {
        return configData;
    }

    public static String readFile(File file) {
        String output = "";
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\Z");
            output = scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static JsonObject getJsonObject(String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public HashMap<String, JsonElement> getRecipes() {
        JsonObject json = toJson(configData).get("recipes").getAsJsonObject();
        HashMap<String, JsonElement> recipes = new HashMap<>();
        for (Map.Entry<String, JsonElement> recipe : json.entrySet()) {
            recipes.put(recipe.getKey(), new JsonParser().parse(recipe.getValue().getAsString()));
        }
        return recipes;
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
