package wraith.waystones.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import wraith.waystones.Waystones;
import wraith.waystones.item.LocalVoidItem;
import wraith.waystones.mixin.StructurePoolAccessor;
import wraith.waystones.screen.AbyssScreenHandler;
import wraith.waystones.screen.PocketWormholeScreenHandler;
import wraith.waystones.screen.WaystoneBlockScreenHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public final class Utils {

    private Utils() {
    }

    public static final Random random = new Random();
    public static final DecimalFormat df = new DecimalFormat("#.##");
    private static final RegistryKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = RegistryKey.of(
            Registry.STRUCTURE_PROCESSOR_LIST_KEY, new Identifier("minecraft", "empty"));

    public static int getRandomIntInRange(int min, int max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return random.nextInt((max - min) + 1) + min;
    }

    public static Identifier ID(String id) {
        return new Identifier(Waystones.MOD_ID, id);
    }

    public static String generateWaystoneName(String id) {
        return id == null || "".equals(id) ? generateUniqueId() : id;
    }

    private static String generateUniqueId() {
        if (random.nextDouble() < 1e-4) {
            return "DeatHunter was here";
        }
        var sb = new StringBuilder();
        char[] vowels = {'a', 'e', 'i', 'o', 'u'};
        char[] consonants = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
        for (int i = 0; i < 4; ++i) {
            var consonant = consonants[Utils.random.nextInt(consonants.length)];
            if (i == 0) {
                consonant = Character.toUpperCase(consonant);
            }
            sb.append(consonant);
            sb.append(vowels[Utils.random.nextInt(vowels.length)]);
        }
        return sb.toString();
    }

    public static void addToStructurePool(MinecraftServer server, Identifier village, Identifier waystone, int weight) {

        RegistryEntry<StructureProcessorList> emptyProcessorList = server.getRegistryManager()
                .get(Registry.STRUCTURE_PROCESSOR_LIST_KEY)
                .entryOf(EMPTY_PROCESSOR_LIST_KEY);

        var poolGetter = server.getRegistryManager()
                .get(Registry.STRUCTURE_POOL_KEY)
                .getOrEmpty(village);

        if (poolGetter.isEmpty()) {
            Waystones.LOGGER.error("Cannot add to " + village + " as it cannot be found!");
            return;
        }
        var pool = poolGetter.get();

        var pieceList = ((StructurePoolAccessor) pool).getElements();
        var piece = StructurePoolElement.ofProcessedSingle(waystone.toString(), emptyProcessorList).apply(StructurePool.Projection.RIGID);

        var list = new ArrayList<>(((StructurePoolAccessor) pool).getElementCounts());
        list.add(Pair.of(piece, weight));
        ((StructurePoolAccessor) pool).setElementCounts(list);

        for (int i = 0; i < weight; ++i) {
            pieceList.add(piece);
        }
    }

    //Values from https://minecraft.gamepedia.com/Experience
    public static long determineLevelXP(final PlayerEntity player) {
        int level = player.experienceLevel;
        long total = player.totalExperience;
        if (level <= 16) {
            total += (long) (Math.pow(level, 2) + 6L * level);
        } else if (level <= 31) {
            total += (long) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            total += (long) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        return total;
    }

    public static int getCost(Vec3d startPos, Vec3d endPos, String startDim, String endDim) {
        var config = Config.getInstance();
        float cost = config.baseTeleportCost();
        if (startDim.equals(endDim)) {
            cost += Math.max(0, startPos.add(0, 0.5, 0).distanceTo(endPos) - 1.4142) * config.extraCostPerBlock();
        } else {
            cost *= config.perDimensionMultiplier();
        }
        return Math.round(cost);
    }

    public static boolean canTeleport(PlayerEntity player, String hash, boolean takeCost) {
        var config = Config.getInstance();
        String cost = config.teleportType();
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            return false;
        }
        int amount = getCost(player.getPos(), Vec3d.ofCenter(waystone.way_getPos()), Utils.getDimensionName(player.world), waystone.getWorldName());
        if (player.isCreative()) {
            return true;
        }
        switch (cost) {
            case "hp":
            case "health":
                if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                    return false;
                }
                if (takeCost) {
                    player.damage(DamageSource.OUT_OF_WORLD, amount);
                }
                return true;
            case "hunger":
            case "saturation":
                var hungerManager = player.getHungerManager();
                var hungerAndExhaustion = hungerManager.getFoodLevel() + hungerManager.getSaturationLevel();
                if (hungerAndExhaustion <= 10 || hungerAndExhaustion + hungerManager.getExhaustion() / 4F <= amount) {
                    return false;
                }
                if (takeCost) {
                    hungerManager.addExhaustion(4 * amount);
                }
                return true;
            case "xp":
            case "experience":
                long total = determineLevelXP(player);
                if (total < amount) {
                    return false;
                }
                if (takeCost) {
                    player.addExperience(-amount);
                }
                return true;
            case "level":
                if (player.experienceLevel < amount) {
                    return false;
                }
                if (takeCost) {
                    player.addExperienceLevels(-amount);
                }
                return true;
            case "item":
                Identifier itemId = Config.getInstance().teleportCostItem();
                Item item = Registry.ITEM.get(itemId);
                if (!containsItem(player.getInventory(), item, amount)) {
                    return false;
                }
                if (takeCost) {
                    removeItem(player.getInventory(), Registry.ITEM.get(itemId), amount);

                    if (player.world.isClient || Waystones.WAYSTONE_STORAGE == null) {
                        return true;
                    }
                    var waystoneBE = waystone.getEntity();
                    if (waystoneBE == null) {
                        return true;
                    }
                    ArrayList<ItemStack> oldInventory = new ArrayList<>(waystoneBE.getInventory());
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
                    waystoneBE.setInventory(oldInventory);
                }
                return true;
            default:
                return true;
        }

    }

    public static boolean containsItem(PlayerInventory inventory, Item item, int maxAmount) {
        int amount = 0;
        for (ItemStack stack : inventory.main) {
            if (stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.offHand) {
            if (stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        return amount >= maxAmount;
    }

    public static void removeItem(PlayerInventory inventory, Item item, int totalAmount) {
        for (ItemStack stack : inventory.main) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.offHand) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.decrement(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
    }

    public static String getSHA256(String data) {
        try {
            return Arrays.toString(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDimensionName(World world) {
        return world.getRegistryKey().getValue().toString();
    }

    public static TeleportSources getTeleportSource(PlayerEntity player) {
        if (player.currentScreenHandler instanceof AbyssScreenHandler) {
            return TeleportSources.ABYSS_WATCHER;
        } else if (player.currentScreenHandler instanceof PocketWormholeScreenHandler) {
            return TeleportSources.POCKET_WORMHOLE;
        } else if (player.currentScreenHandler instanceof WaystoneBlockScreenHandler) {
            return TeleportSources.WAYSTONE;
        } else {
            for (var hand : Hand.values()) {
                if (!(player.getStackInHand(hand).getItem() instanceof LocalVoidItem)) continue;
                return TeleportSources.LOCAL_VOID;
            }
        }
        return null;
    }
}
