package wraith.fwaystones.util;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.integration.lithostitched.LithostitchedPlugin;
import wraith.fwaystones.mixin.ExhaustionAccessor;
import wraith.fwaystones.mixin.StructurePoolAccessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Utils {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static final Random random = new Random();

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

    public static ResourceLocation ID(String id) {
        return ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, id);
    }

    public static String generateWaystoneName(String id) {
        return id == null || id.isEmpty() ? generateUniqueId() : id;
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
        var pool = server.registryAccess()
            .lookupOrThrow(Registries.TEMPLATE_POOL)
            .getValue(village);

        if (pool == null) {
            FabricWaystones.LOGGER.error("Cannot add to " + village + " as it cannot be found!");
            return;
        }

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            var pieces = LithostitchedPlugin.createPieces(waystone.toString());
            for (StructurePoolElement piece : pieces) {
                addPieceToPool(piece, ((StructurePoolAccessor) pool), weight);
            }
        } else {
            var piece = StructurePoolElement.single(waystone.toString()).apply(StructureTemplatePool.Projection.RIGID);
            addPieceToPool(piece, ((StructurePoolAccessor) pool), weight);
        }
    }

    private static void addPieceToPool(StructurePoolElement element, StructurePoolAccessor accessor, int weight) {
        var pieceList = accessor.getElements();
        var list = new ArrayList<>(accessor.getElementWeights());
        list.add(Pair.of(element, weight));
        accessor.setElementWeights(list);

        for (int i = 0; i < weight; ++i) {
            pieceList.add(element);
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
        var config = FabricWaystones.CONFIG.teleportation_cost;
        if (config.cost_type().equals(FWConfigModel.CostType.NONE)) {
            return 0;
        }
        float cost = config.base_cost();
        if (startDim.equals(endDim)) {
            cost += (float) (Math.max(0, startPos.add(0, 0.5, 0).distanceTo(endPos) - 1.4142) * config.cost_per_block_distance());
        } else {
            cost *= config.cost_multiplier_between_dimensions();
        }
        return Math.round(cost);
    }

    public static boolean isDimensionBlacklisted(String dim, boolean isSource) {
        List<String> blacklist = isSource ? FabricWaystones.CONFIG.disable_teleportation_from_dimensions() : FabricWaystones.CONFIG.disable_teleportation_to_dimensions();

        if (blacklist.contains(dim)) {
            return true;
        }

        String dimNamespace = dim.split(":")[0];
        return blacklist.stream().anyMatch(blacklistedDim -> {
            if (blacklistedDim.equals(dim) || blacklistedDim.equals("*")) return true;
            String[] paths = blacklistedDim.split(":");
            if (paths.length != 2) return false;
            return paths[0].equals(dimNamespace) && paths[1].equals("*");
        });
    }

    public static boolean canTeleport(Player player, String hash, TeleportSources source, boolean takeCost) {
        FWConfigModel.CostType cost = FabricWaystones.CONFIG.teleportation_cost.cost_type();
        var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            player.displayClientMessage(Component.translatable("fwaystones.no_teleport.invalid_waystone"), true);
            return false;
        }
        var sourceDim = getDimensionName(player.level());
        var destDim = waystone.getWorldName();
        if (source == TeleportSources.VOID_TOTEM) {
            return true;
        }
        if (!FabricWaystones.CONFIG.ignore_dimension_blacklists_if_same_dimension() || !sourceDim.equals(destDim)) {
            if (isDimensionBlacklisted(sourceDim, true)) {
                player.displayClientMessage(Component.translatable("fwaystones.no_teleport.blacklisted_dimension_source"), true);
                return false;
            }
            if (isDimensionBlacklisted(destDim, false)) {
                player.displayClientMessage(Component.translatable("fwaystones.no_teleport.blacklisted_dimension_destination"), true);
                return false;
            }
        }
        if (source == TeleportSources.LOCAL_VOID && FabricWaystones.CONFIG.free_local_void_teleport()) {
            return true;
        }
        int amount = getCost(player.position(), Vec3.atCenterOf(waystone.way_getPos()), sourceDim, destDim);
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }
        switch (cost) {
            case HEALTH -> {
                if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.health"), true);
                    return false;
                }
                if (takeCost) {
                    player.hurtServer((ServerLevel) player.level(), player.level().damageSources().magic(), amount);
                }
                return true;
            }
            case HUNGER -> {
                var hungerManager = player.getFoodData();
                var hungerAndExhaustion = hungerManager.getFoodLevel() + hungerManager.getSaturationLevel();
                if (hungerAndExhaustion <= 10 || hungerAndExhaustion + ((ExhaustionAccessor) hungerManager).getExhaustion() / 4F <= amount) {
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.hunger"), true);
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
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.xp"), true);
                    return false;
                }
                if (takeCost) {
                    player.giveExperiencePoints(-amount);
                }
                return true;
            }
            case LEVEL -> {
                if (player.experienceLevel < amount) {
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.level"), true);
                    return false;
                }
                if (takeCost) {
                    player.giveExperienceLevels(-amount);
                }
                return true;
            }
            case ITEM -> {
                ResourceLocation itemId = getTeleportCostItem();
                Item item = BuiltInRegistries.ITEM.getValue(itemId);
                if (!containsItem(player.getInventory(), item, amount)) {
                    player.displayClientMessage(Component.translatable("fwaystones.no_teleport.item"), true);
                    return false;
                }
                if (takeCost) {
                    removeItem(player.getInventory(), item, amount);

                    if (player.level().isClientSide() || FabricWaystones.WAYSTONE_STORAGE == null) {
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
                            stack.grow(amount);
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

    public static boolean containsItem(Inventory inventory, Item item, int maxAmount) {
        int amount = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem().equals(item)) {
                amount += stack.getCount();
            }
        }
        return amount >= maxAmount;
    }

    public static void removeItem(Inventory inventory, Item item, int totalAmount) {
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
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
            FabricWaystones.LOGGER.error(e.getMessage());
        }
        return "";
    }

    public static String getDimensionName(Level world) {
        return world.dimension().location().toString();
    }

    public static int getRandomColor() {
        return random.nextInt(0xFFFFFF);
    }

    @Nullable
    public static ResourceLocation getTeleportCostItem() {
        if (FabricWaystones.CONFIG.teleportation_cost.cost_type() == FWConfigModel.CostType.ITEM) {
            String[] item = FabricWaystones.CONFIG.teleportation_cost.cost_item().split(":");
            return (item.length == 2) ? ResourceLocation.fromNamespaceAndPath(item[0], item[1]) : ResourceLocation.parse(item[0]);
        }
        return null;
    }

    @Nullable
    public static ResourceLocation getDiscoverItem() {
        var discoverStr = FabricWaystones.CONFIG.discover_with_item();
        if (discoverStr.equals("none")) {
            return null;
        }
        String[] item = discoverStr.split(":");
        return (item.length == 2) ? ResourceLocation.fromNamespaceAndPath(item[0], item[1]) : ResourceLocation.parse(item[0]);
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
