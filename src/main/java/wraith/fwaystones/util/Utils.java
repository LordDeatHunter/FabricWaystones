package wraith.fwaystones.util;

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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.mixin.StructurePoolAccessor;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public final class Utils {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static final Random random = new Random();
    private static final RegistryKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = RegistryKey.of(
        Registry.STRUCTURE_PROCESSOR_LIST_KEY, new Identifier("minecraft", "empty"));

    private Utils() {
    }

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
        return new Identifier(FabricWaystones.MOD_ID, id);
    }

    public static String generateWaystoneName(String id) {
        return id == null || "".equals(id) ? generateUniqueId() : id;
    }

    private static String generateUniqueId() {
        if (random.nextDouble() < 1e-4) {
            return "DeatHunter was here";
        }
        var sb = new StringBuilder();
        char[] vowels = { 'a', 'e', 'i', 'o', 'u' };
        char[] consonants = { 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z' };
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
            FabricWaystones.LOGGER.error("Cannot add to " + village + " as it cannot be found!");
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
        var config = FabricWaystones.CONFIG.teleportation_cost;
        float cost = config.base_cost();
        if (startDim.equals(endDim)) {
            cost += Math.max(0, startPos.add(0, 0.5, 0).distanceTo(endPos) - 1.4142) * config.cost_per_block_distance();
        } else {
            cost *= config.cost_multiplier_between_dimensions();
        }
        return Math.round(cost);
    }

    public static boolean canTeleport(PlayerEntity player, String hash, boolean takeCost) {
        FWConfigModel.CostType cost = FabricWaystones.CONFIG.teleportation_cost.cost_type();
        var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            player.sendMessage(Text.translatable("fwaystones.no_teleport.invalid_waystone"), true);
            return false;
        }
        var sourceDim = getDimensionName(player.world);
        var destDim = waystone.getWorldName();
        if (!FabricWaystones.CONFIG.ignore_dimension_blacklists_if_same_dimension() || !sourceDim.equals(destDim)) {
            if (FabricWaystones.CONFIG.disable_teleportation_from_dimensions().contains(sourceDim)) {
                player.sendMessage(Text.translatable("fwaystones.no_teleport.blacklisted_dimension_source"), true);
                return false;
            }
            if (FabricWaystones.CONFIG.disable_teleportation_to_dimensions().contains(destDim)) {
                player.sendMessage(Text.translatable("fwaystones.no_teleport.blacklisted_dimension_destination"), true);
                return false;
            }
        }
        int amount = getCost(player.getPos(), Vec3d.ofCenter(waystone.way_getPos()), sourceDim, destDim);
        if (player.isCreative()) {
            return true;
        }
        switch (cost) {
            case HEALTH -> {
                if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                    player.sendMessage(Text.translatable("fwaystones.no_teleport.health"), true);
                    return false;
                }
                if (takeCost) {
                    player.damage(DamageSource.MAGIC, amount);
                }
                return true;
            }
            case HUNGER -> {
                var hungerManager = player.getHungerManager();
                var hungerAndExhaustion = hungerManager.getFoodLevel() + hungerManager.getSaturationLevel();
                if (hungerAndExhaustion <= 10 || hungerAndExhaustion + hungerManager.getExhaustion() / 4F <= amount) {
                    player.sendMessage(Text.translatable("fwaystones.no_teleport.hunger"), true);
                    return false;
                }
                if (takeCost) {
                    hungerManager.addExhaustion(4 * amount);
                }
                return true;
            }
            case EXPERIENCE -> {
                long total = determineLevelXP(player);
                if (total < amount) {
                    player.sendMessage(Text.translatable("fwaystones.no_teleport.xp"), true);
                    return false;
                }
                if (takeCost) {
                    player.addExperience(-amount);
                }
                return true;
            }
            case LEVEL -> {
                if (player.experienceLevel < amount) {
                    player.sendMessage(Text.translatable("fwaystones.no_teleport.level"), true);
                    return false;
                }
                if (takeCost) {
                    player.addExperienceLevels(-amount);
                }
                return true;
            }
            case ITEM -> {
                Identifier itemId = getTeleportCostItem();
                Item item = Registry.ITEM.get(itemId);
                if (!containsItem(player.getInventory(), item, amount)) {
                    player.sendMessage(Text.translatable("fwaystones.no_teleport.item"), true);
                    return false;
                }
                if (takeCost) {
                    removeItem(player.getInventory(), item, amount);

                    if (player.world.isClient || FabricWaystones.WAYSTONE_STORAGE == null) {
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
                        oldInventory.add(new ItemStack(item, amount));
                    }
                    waystoneBE.setInventory(oldInventory);
                }
                return true;
            }
            default -> {
                return true;
            }
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

    public static int getRandomColor() {
        return random.nextInt(0xFFFFFF);
    }

    @Nullable
    public static Identifier getTeleportCostItem() {
        if (FabricWaystones.CONFIG.teleportation_cost.cost_type() == FWConfigModel.CostType.ITEM) {
            String[] item = FabricWaystones.CONFIG.teleportation_cost.cost_item().split(":");
            return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
        }
        return null;
    }

    @Nullable
    public static Identifier getDiscoverItem() {
        var discoverStr = FabricWaystones.CONFIG.discover_with_item();
        if (discoverStr.equals("none")) {
            return null;
        }
        String[] item = discoverStr.split(":");
        return (item.length == 2) ? new Identifier(item[0], item[1]) : new Identifier(item[0]);
    }

    public static boolean isSubSequence(String mainString, String searchString) {
        int j = 0;
        for (int i = 0; i < mainString.length() && j < searchString.length(); ++i) {
            if (mainString.charAt(i) == searchString.charAt(j))
                ++j;
            if (j == searchString.length()) return true;
        }
        return false;
    }
}
