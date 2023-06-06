package wraith.fwaystones.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.item.LocalVoidItem;
import wraith.fwaystones.mixin.StructurePoolAccessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public final class Utils {
    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static final Random random = new Random();
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("minecraft", "empty"));
    private Utils() {}
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
    public static ResourceLocation ID(String id) {
        return new ResourceLocation(Waystones.MOD_ID, id);
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
    public static void addToStructurePool(MinecraftServer server, ResourceLocation village, ResourceLocation waystone, int weight) {
        Holder<StructureProcessorList> emptyProcessorList = server.registryAccess()
                .ownedRegistryOrThrow(Registry.PROCESSOR_LIST_REGISTRY)
                .getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);
        var poolGetter = server.registryAccess()
                .ownedRegistryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                .getOptional(village);
        if (poolGetter.isEmpty()) {
            Waystones.LOGGER.error("Cannot add to " + village + " as it cannot be found!");
            return;
        }
        var pool = poolGetter.get();
        var pieceList = ((StructurePoolAccessor) pool).getElements();
        var piece = StructurePoolElement.single(waystone.toString(), emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);
        var list = new ArrayList<>(((StructurePoolAccessor) pool).getElementCounts());
        list.add(Pair.of(piece, weight));
        ((StructurePoolAccessor) pool).setElementCounts(list);
        for (int i = 0; i < weight; ++i) {
            pieceList.add(piece);
        }
    }
    //Values from https://minecraft.gamepedia.com/Experience
    public static long determineLevelXP(final Player player) {
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
    public static int getCost(Vec3 startPos, Vec3 endPos, String startDim, String endDim) {
        var config = Waystones.CONFIG.teleportation_cost;
        float cost = config.base_cost;
        if (startDim.equals(endDim)) {
            cost += Math.max(0, startPos.add(0, 0.5, 0).distanceTo(endPos) - 1.4142) * config.cost_per_block_distance;
        } else {
            cost *= config.cost_multiplier_between_dimensions;
        }
        return Math.round(cost);
    }
    public static boolean canTeleport(Player player, String hash, boolean takeCost) {
        return false;
        /*
        ConfigModel.CostType cost = Waystones.CONFIG.teleportation_cost.cost_type;
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            player.displayClientMessage(Component.translatable("fwaystones.no_teleport.invalid_waystone"), true);
            return false;
        }
        var sourceDim = getDimensionName(player.level);
        var destDim = waystone.getWorldName();
        if (!Waystones.CONFIG.ignore_dimension_blacklists_if_same_dimension || !sourceDim.equals(destDim)) {
            if (Waystones.CONFIG.disable_teleportation_from_dimensions.contains(sourceDim)) {
                player.displayClientMessage(Component.translatable("fwaystones.no_teleport.blacklisted_dimension_source"), true);
                return false;
            }
            if (Waystones.CONFIG.disable_teleportation_to_dimensions.contains(destDim)) {
                player.displayClientMessage(Component.translatable("fwaystones.no_teleport.blacklisted_dimension_destination"), true);
                return false;
            }
        }
        int amount = getCost(player.position(), Vec3.atCenterOf(waystone.way_getPos()), sourceDim, destDim);
        if (player.isCreative()) {
            return true;
        }
        switch (cost) {
            case HEALTH -> {
                if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.health"), true);
                    return false;
                }
                if (takeCost) {
                    player.hurt(player.getLevel().getDamageSources().magic(), amount);
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
                Item item = Registries.ITEM.get(itemId);
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
        }*/
    }
    public static boolean containsItem(Inventory inventory, Item item, int maxAmount) {
        int amount = 0;
        for (ItemStack stack : inventory.items) {
            if (stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.offhand) {
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
    public static void removeItem(Inventory inventory, Item item, int totalAmount) {
        for (ItemStack stack : inventory.items) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.shrink(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.shrink(totalAmount);
                totalAmount -= amount;
            }
            if (totalAmount <= 0) {
                return;
            }
        }
        for (ItemStack stack : inventory.armor) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();
                stack.shrink(totalAmount);
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

    public static String getDimensionName(Level level) {
        return level.dimensionTypeId().toString();
    }

    public static TeleportSources getTeleportSource(Player player) {
        /* TODO:
            if (player.containerMenu instanceof AbyssScreenHandler) {
                return TeleportSources.ABYSS_WATCHER;
            } else if (player.containerMenu instanceof PocketWormholeScreenHandler) {
                return TeleportSources.POCKET_WORMHOLE;
            } else if (player.containerMenu instanceof WaystoneBlockScreenHandler) {
                return TeleportSources.WAYSTONE;
            } else {
                for (var hand : InteractionHand.values()) {
                    if (!(player.getItemInHand(hand).getItem() instanceof LocalVoidItem)) continue;
                    return TeleportSources.LOCAL_VOID;
                }
            }*/
        for (var hand : InteractionHand.values()) {
            if (!(player.getItemInHand(hand).getItem() instanceof LocalVoidItem)) continue;
            return TeleportSources.LOCAL_VOID;
        }
        return null;
    }

    public static int getRandomColor() {
        return random.nextInt(0xFFFFFF);
    }

    @Nullable
    public static ResourceLocation getTeleportCostItem() {
        if (Waystones.CONFIG.teleportation_cost.cost_type == ConfigModel.CostType.ITEM) {
            String[] item = Waystones.CONFIG.teleportation_cost.cost_item.split(":");
            return (item.length == 2) ? new ResourceLocation(item[0], item[1]) : new ResourceLocation(item[0]);
        }
        return null;
    }

    @Nullable
    public static ResourceLocation getDiscoverItem() {
        var discoverStr = Waystones.CONFIG.discover_with_item;
        if (discoverStr.equals("none")) {
            return null;
        }
        String[] item = discoverStr.split(":");
        return (item.length == 2) ? new ResourceLocation(item[0], item[1]) : new ResourceLocation(item[0]);
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
