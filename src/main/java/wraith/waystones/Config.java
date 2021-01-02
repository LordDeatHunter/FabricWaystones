package wraith.waystones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    protected class configdata
    {

    }

    /*
     * Singleton :/
     */
    private static Config instance = null;

    public static Config getInstance()
    {
        if(instance == null)
        {
            instance = new Config();
        }
        return instance;
    }

    /*
     * Getters and Setters for Config
     */
    public boolean generateInVillages() {
        return villageGen;
    }

    public Identifier teleportCostItem() {
        return costItem;
    }

    public String teleportType()
    {
        return teleportType;
    }

    public int teleportCost() {
        return costTotal;
    }

    public boolean canGlobalDiscover() {
        return globalDiscover;
    }

    public JsonObject getRecipe() {
        return recipe;
    }

    private JsonObject recipe;
    private boolean globalDiscover;
    private String teleportType;
    private int costTotal;
    private Identifier costItem;
    private boolean villageGen;

    private static final String CONFIG_FILE = "config/waystones/config.json";
    private static final String RECIPE_FILE = "config/waystones/recipe.json";

    private Config()
    {
        recipe = null;
        globalDiscover = false;
        teleportType = "none";
        costTotal = 0;
        costItem = new Identifier("empty");
        villageGen = true;
        createDefaults();
    }

    private void createDefaults()
    {
        //Config File
        JsonObject defaultConf  = new JsonObject();
        defaultConf.addProperty("global_discover", false);
        defaultConf.addProperty("cost_type", "xp");
        defaultConf.addProperty("cost_item", "minecraft:ender_pearl");
        defaultConf.addProperty("cost_amount", 1);
        defaultConf.addProperty("village_generation", true);
        //Recipe File
        JsonObject defaultRec = new JsonObject();
        defaultRec.addProperty("type", "minecraft:crafting_shaped");
        JsonArray array = new JsonArray();
        array.add("BBB"); array.add("BEB"); array.add("BBB");
        defaultRec.add("pattern", array);
        JsonObject key = new JsonObject();
        JsonObject b = new JsonObject();
        JsonObject e = new JsonObject();
        b.addProperty("item", "minecraft:stone_bricks");
        e.addProperty("item", "minecraft:ender_pearl");
        key.add("B", b);
        key.add("E", e);
        defaultRec.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("item", "waystones:waystone");
        result.addProperty("count", 1);
        defaultRec.add("result", result);
        //Create Them
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        createFile(CONFIG_FILE, gson.toJson(defaultConf), false);
        createFile(RECIPE_FILE, gson.toJson(defaultRec), false);
    }

    public void loadConfig() {
        loadConfig(getJsonObject(readFile(new File(CONFIG_FILE))), getJsonObject(readFile(new File(RECIPE_FILE))));
    }

    public void loadConfig(JsonObject config, JsonObject recipe)
    {
        this.recipe = recipe;
        try{
            this.globalDiscover = config.get("global_discover").getAsBoolean();
            if (config.has("cost_type") && config.has("cost_amount")) {
                teleportType = config.get("cost_type").getAsString();
                costTotal = Math.abs(config.get("cost_amount").getAsInt());
                villageGen = config.get("village_generation").getAsBoolean();
                if("item".equals(teleportType))
                {
                    String[] item = config.get("cost_item").getAsString().split(":");
                    if (item.length == 2) {
                        costItem = new Identifier(item[0], item[1]);
                    } else {
                        costItem = new Identifier(item[0]);
                    }
                }   
            }
        }
        catch(ClassCastException ex)
        {
            System.out.println("Error with config...");
        }
        catch(IllegalStateException ex)
        {
            System.out.println("Error with recipe...");
        }
    }


    private void createFile(String path, String contents, boolean overwrite) {
        File file = new File(path);
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
        if (contents == null || "".equals(contents)) {
            return;
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompoundTag toCompoundTag()
    {
        CompoundTag tag = new CompoundTag();
        //todo: make json object
        //JsonObject config = new JsonObject();
        String contents = Config.readFile(new File(CONFIG_FILE));
        tag.putString("config", contents);
        contents = recipe.toString();
        tag.putString("recipe", contents);
        return tag;
    }

    public static String readFile(File file) {
        String output = "";
        try(Scanner scanner = new Scanner(file)) {
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

}
