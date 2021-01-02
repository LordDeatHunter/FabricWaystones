package wraith.waystones;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Utils {

    public static final Random random = new Random(Calendar.getInstance().getTimeInMillis());
    public static int getRandomIntInRange(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static Identifier ID(String id) {
        return new Identifier(Waystones.MOD_ID, id);
    }

    public static String generateWaystoneName(String id) {
        String out;
        if (!"".equals(id) && !Waystones.WAYSTONE_DATABASE.containsWaystone(id)) {
            out = id;
        } else {
            do {
                out = generateUniqueId();
            } while(Waystones.WAYSTONE_DATABASE.containsWaystone(out));
        }
        return out;
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
            total += (long) (Math.pow(level, 2) + 6 * level);
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

    public static boolean canTeleport(PlayerEntity player, WaystoneBlockEntity waystone) {
        String cost = Config.getInstance().teleportType();
        int amount = Config.getInstance().teleportCost();
        if(player.isCreative()) {
            return true;
        }
        switch(cost)
        {
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
                Identifier item = Config.getInstance().teleportCostItem();
                if (!containsItem(player.inventory, Registry.ITEM.get(item), amount)) {
                    return false;
                }
                removeItem(player.inventory, Registry.ITEM.get(item), amount);

                if (waystone == null) {
                    return true;
                }
                ArrayList<ItemStack> oldInventory = new ArrayList<>(waystone.inventory);
                oldInventory.add(new ItemStack(Registry.ITEM.get(item), amount));
                waystone.inventory = DefaultedList.ofSize(oldInventory.size(), ItemStack.EMPTY);
                for (int i = 0; i < oldInventory.size(); i++) {
                    waystone.inventory.set(i, oldInventory.get(i));
                }
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

}
