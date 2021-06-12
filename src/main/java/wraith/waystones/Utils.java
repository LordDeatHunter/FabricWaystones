package wraith.waystones;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import wraith.waystones.block.WaystoneBlockEntity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {

    public static final Random random = new Random();
    public static int getRandomIntInRange(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static Identifier ID(String id) {
        return new Identifier(Waystones.MOD_ID, id);
    }

    public static String generateWaystoneName(String id) {
        return id == null || "".equals(id) ? generateUniqueId() : id;
    }

    private static String generateUniqueId() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Character> vowels = new ArrayList<Character>(){{
            add('a');
            add('e');
            add('i');
            add('o');
            add('u');
        }};
        char c;
        do {
            c = (char)Utils.getRandomIntInRange(65, 90);
        } while(c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U');
        sb.append(c);
        sb.append(vowels.get(Utils.random.nextInt(5)));
        for (int i = 0; i < 3; ++i) {
            do {
                c = (char)Utils.getRandomIntInRange(97, 122);
            } while(c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
            sb.append(c);
            sb.append(vowels.get(Utils.random.nextInt(5)));
        }
        return sb.toString();
    }


    public static StructurePool tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            ModifiableStructurePool modPool = new ModifiableStructurePool(pool);
            modPool.addStructurePoolElement(StructurePoolElement.method_30426(elementId, StructureProcessorLists.EMPTY).apply(projection), weight);
            return modPool.getStructurePool();
        }
        return pool;
    }

    public static StructurePool tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection) {
        if(targetPool.equals(pool.getId())) {
            ModifiableStructurePool modPool = new ModifiableStructurePool(pool);
            modPool.addStructurePoolElement(StructurePoolElement.method_30426(elementId, StructureProcessorLists.EMPTY).apply(projection));
            return modPool.getStructurePool();
        }
        return pool;
    }

    //Values from https://minecraft.gamepedia.com/Experience
    public static long determineLevelXP(final PlayerEntity player)
    {
        int level = player.experienceLevel;
        long total = player.totalExperience;
        if(level <= 16)
        {
            total += (long) (Math.pow(level, 2) + 6L * level);
        }
        else if(level <= 31)
        {
            total += (long) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        }
        else {
            total += (long) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        return total;
    }

    public static boolean canTeleport(PlayerEntity player, String hash) {
        String cost = Config.getInstance().teleportType();
        int amount = Config.getInstance().teleportCost();
        if(player.isCreative()) {
            return true;
        }
        switch(cost) {
            case "none":
                return true;
            case "xp":
                long total = determineLevelXP(player);
                if(total < amount) {
                    return false;
                }
                player.addExperience(-amount);
                return true;
            case "level":
                if (player.experienceLevel < amount) {
                    return false;
                }
                player.experienceLevel -= amount;
                return true;
            case "item":
                Identifier itemId = Config.getInstance().teleportCostItem();
                Item item = Registry.ITEM.get(itemId);
                if (!containsItem(player.inventory, item, amount)) {
                    return false;
                }
                removeItem(player.inventory, Registry.ITEM.get(itemId), amount);

                if (player.world.isClient || Waystones.WAYSTONE_STORAGE == null) {
                    return true;
                }
                WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone == null) {
                    return true;
                }
                ArrayList<ItemStack> oldInventory = new ArrayList<>(waystone.getInventory());
                boolean found = false;
                for (ItemStack stack : oldInventory) {
                    if (stack.getItem() == item) {
                        stack.increment(amount);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    oldInventory.add(new ItemStack(Registry.ITEM.get(itemId), amount));
                }
                waystone.setInventory(oldInventory);
                return true;
            default:
                return false;
        }
        
    }

    private static boolean containsItem(PlayerInventory inventory, Item item, int maxAmount) {
        int amount = 0;
        for (ItemStack stack : inventory.main) {
            if(stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.offHand) {
            if(stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.armor) {
            if(stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        return amount >= maxAmount;
    }

    private static void removeItem(PlayerInventory inventory, Item item, int totalAmount) {
        for (ItemStack stack : inventory.main) {
            if(stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.offHand) {
            if(stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if(stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
    }

    public static JsonObject createRecipe(String patternString, HashMap<String, String> itemMap, String output, int amount) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shaped");

        JsonArray jsonArray = new JsonArray();

        String[] pattern = patternString.split("_");

        jsonArray.add(pattern[0]);
        if (pattern.length > 1) {
            jsonArray.add(pattern[1]);
        }
        if (pattern.length > 2) {
            jsonArray.add(pattern[2]);
        }

        json.add("pattern", jsonArray);

        JsonObject individualKey;
        JsonObject keyList = new JsonObject();

        for (Map.Entry<String, String> item : itemMap.entrySet()) {
            individualKey = new JsonObject();
            individualKey.addProperty("item", item.getValue());
            keyList.add(item.getKey(), individualKey);
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("item", output);
        result.addProperty("count", amount);
        json.add("result", result);

        return json;
    }

    public static String getSHA256(String data) {
        try {
            return Arrays.toString(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
