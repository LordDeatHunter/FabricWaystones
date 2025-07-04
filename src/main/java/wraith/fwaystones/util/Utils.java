package wraith.fwaystones.util;

import com.mojang.datafixers.util.Pair;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.*;
import wraith.fwaystones.api.teleport.TeleportAction;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.integration.lithostitched.LithostitchedPlugin;
import wraith.fwaystones.mixin.StructurePoolAccessor;
import wraith.fwaystones.registry.WaystoneDataComponents;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;

public final class Utils {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static final Random random = new Random();
    private static final RegistryKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = RegistryKey.of(
        RegistryKeys.PROCESSOR_LIST, Identifier.of("empty"));

    public static int getRandomIntInRange(int min, int max) {
        if (min == max) return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        return random.nextInt((max - min) + 1) + min;
    }

    public static String generateWaystoneName(String id) {
        return id == null || id.isEmpty() ? generateUniqueId() : id;
    }

    private static String generateUniqueId() {
        if (random.nextDouble() < 1e-4) return "DeatHunter was here";

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
        var poolGetter = server.getRegistryManager()
            .get(RegistryKeys.TEMPLATE_POOL)
            .getOrEmpty(village);

        if (poolGetter.isEmpty()) {
            if (!FabricWaystones.CONFIG.generalLoggingLevel().equals(FWConfigModel.LoggingLevel.NONE)) {
                FabricWaystones.LOGGER.error("Cannot add a waystone to {} as it cannot be found!", village);
            }

            return;
        }

        var pool = ((StructurePoolAccessor) poolGetter.get());

        if (FabricLoader.getInstance().isModLoaded("lithostitched")) {
            for (var piece : LithostitchedPlugin.createPieces(waystone.toString())) {
                addPieceToPool(piece, pool, weight);
            }
        } else {
            var piece = StructurePoolElement.ofSingle(waystone.toString()).apply(StructurePool.Projection.RIGID);
            addPieceToPool(piece, pool, weight);
        }
    }

    private static void addPieceToPool(StructurePoolElement piece, StructurePoolAccessor pool, int weight) {
        var list = new ArrayList<>(pool.getElementCounts());
        list.add(Pair.of(piece, weight));
        pool.setElementCounts(list);

        var pieceList = pool.getElements();

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
        var config = FabricWaystones.CONFIG.teleportCost;
        if (config.type().equals(FWConfigModel.CostType.NONE)) return 0;

        float cost = config.baseAmount();

        if (startDim.equals(endDim) || config.allowBothMultipliers()) {
            cost += (float) (Math.max(0, startPos.add(0, 0.5, 0).distanceTo(endPos) - 1.4142) * config.perBlockMultiplier());
        }

        if (!startDim.equals(endDim)) {
            cost *= config.dimensionMultiplier();
        }

        return Math.round(cost);
    }

    public static boolean isDimensionBlacklisted(String dim, boolean isSource) {
        var blacklist = isSource
                ? FabricWaystones.CONFIG.blacklistTeleportFromDimensions()
                : FabricWaystones.CONFIG.blacklistTeleportToDimensions();

        if (blacklist.contains(dim)) return true;

        String dimNamespace = dim.split(":")[0];

        return blacklist.stream().anyMatch(blacklistedDim -> {
            if (blacklistedDim.equals(dim) || blacklistedDim.equals("*")) {
                return true;
            }

            var paths = blacklistedDim.split(":");
            if (paths.length == 2) {
                return paths[0].equals(dimNamespace) && paths[1].equals("*");
            }

            return false;
        });
    }

    //--

    public static boolean teleportPlayer(Entity entity, TeleportAction action, boolean takeCost) {
        if (action == null) return false;

        if (!attemptTeleport(entity, action, takeCost)) return false;

        if (entity instanceof LivingEntity livingEntity) {
            if (!livingEntity.isInCreativeMode() && action.isFrom(TeleportSource.ABYSS_WATCHER)) {
                var stackReference = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(livingEntity, stack -> {
                    var component = stack.get(WaystoneDataComponents.TELEPORTER);

                    return component != null && component.oneTimeUse();
                });

                if (stackReference != null) {
                    var stack = stackReference.get();
                    var data = stack.get(WaystoneDataComponents.TELEPORTER);

                    if (data != null && data.oneTimeUse()) {
                        stackReference.breakStack(stack.copy());

                        stack.decrement(1);

                        stackReference.set(stack);
                    }
                }
            }
        }

        return true;
    }

    private static boolean attemptTeleport(Entity entity, TeleportAction action, boolean takeCost) {
        if (!Utils.canTeleport(entity, action, takeCost)) return false;

        var target = action.createTarget(entity);

        if (entity instanceof PlayerEntity player) {
            WaystonePlayerData.getData(player).teleportCooldown(action.getCooldown());
        }

        var oldPos = entity.getBlockPos();
        if (!oldPos.isWithinDistance(target.pos(), 6) || !entity.getWorld().getRegistryKey().equals(target.world().getRegistryKey())) {
            entity.getWorld().playSound(null, oldPos, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
        }

        entity.teleportTo(target);

        var playerPos = entity.getBlockPos();

        target.world().playSound(null, playerPos, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);

        return true;
    }

    public static boolean canTeleportCostLess(PlayerEntity player, TeleportAction action) {
        return canTeleport(player, action, false);
    }

    public static boolean canTeleport(Entity entity, TeleportAction action, boolean takeCost) {
        PlayerEntity player = (entity instanceof PlayerEntity playerEntity) ? playerEntity : null;

        if (!action.isValid(entity)) return false;
        if (action.isFrom(TeleportSource.VOID_TOTEM)) return true;

        var globalPos = action.getPos(entity.getWorld());

        var costType = FabricWaystones.CONFIG.teleportCost.type();

        var sourceDim = getDimensionName(entity.getWorld());
        var destDim = globalPos.dimension().getValue().toString();

        if (!FabricWaystones.CONFIG.ignoreBlacklistForInterdimensionTravel() || !sourceDim.equals(destDim)) {
            if (isDimensionBlacklisted(sourceDim, true)) {
                if(player != null) player.sendMessage(Text.translatable("fwaystones.no_teleport.blacklisted_dimension_source"), true);
                return false;
            }
            if (isDimensionBlacklisted(destDim, false)) {
                if(player != null) player.sendMessage(Text.translatable("fwaystones.no_teleport.blacklisted_dimension_destination"), true);
                return false;
            }
        }

        if (action.isFrom(TeleportSource.VOID_TOTEM) && FabricWaystones.CONFIG.shouldLocalVoidTeleportBeFree()) {
            return true;
        }

        if (player != null) {
            if (player.isCreative() || player.isSpectator()) return true;

            int amount = getCost(entity.getPos(), globalPos.pos().toCenterPos(), sourceDim, destDim);

            switch (costType) {
                case HEALTH -> {
                    if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                        player.sendMessage(Text.translatable("fwaystones.no_teleport.health"), true);
                        return false;
                    }
                    if (takeCost) {
                        player.damage(player.getWorld().getDamageSources().magic(), amount);
                    }
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
                }
                case LEVEL -> {
                    if (player.experienceLevel < amount) {
                        player.sendMessage(Text.translatable("fwaystones.no_teleport.level"), true);
                        return false;
                    }
                    if (takeCost) {
                        player.addExperienceLevels(-amount);
                    }
                }
                case ITEM -> {
                    var itemId = getTeleportCostItem();
                    var item = Registries.ITEM.get(itemId);

                    if (!containsItem(player.getInventory(), item, amount)) {
                        player.sendMessage(Text.translatable("fwaystones.no_teleport.item"), true);
                        return false;
                    }

                    if (takeCost) {
                        removeItem(player.getInventory(), item, amount);

                        action.addConsumedItems(player.getWorld(), item, amount);
                    }
                }
            }
        }

        return true;
    }

    public static boolean containsItem(PlayerInventory inventory, Item item, int maxAmount) {
        var amount = getAmount(inventory.main, item)
                + getAmount(inventory.offHand, item)
                + getAmount(inventory.main, item);

        return amount >= maxAmount;
    }

    public static int getAmount(List<ItemStack> stacks, Item item) {
        int amount = 0;
        for (var stack : stacks) {
            if (stack.getItem().equals(item)) amount += stack.getCount();
        }
        return amount;
    }

    public static void removeItem(PlayerInventory inventory, Item item, int totalAmount) {
        var amount = new MutableInt(totalAmount);

        if (attemptToRemoveAmount(inventory.main, item, amount)) return;
        if (attemptToRemoveAmount(inventory.offHand, item, amount)) return;

        attemptToRemoveAmount(inventory.armor, item, amount);
    }

    public static boolean attemptToRemoveAmount(List<ItemStack> stacks, Item item, MutableInt totalAmount) {
        for (var stack : stacks) {
            if (stack.getItem().equals(item)) {
                int amount = stack.getCount();

                stack.decrement(totalAmount.getValue());

                totalAmount.subtract(amount);
            }

            if (totalAmount.getValue() <= 0) return true;
        }

        return false;
    }

    public static String getSHA256(String data) {
        try {
            return Arrays.toString(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            FabricWaystones.LOGGER.error(e.getMessage());
        }
        return "";
    }

    public static String getDimensionName(World world) {
        return world.getRegistryKey().getValue().toString();
    }

    public static int getRandomColor() {
        return random.nextInt(0xFFFFFF);
    }

    @Nullable
    public static Identifier getTeleportCostItem() {
        var cost = FabricWaystones.CONFIG.teleportCost;

        return (cost.type() == FWConfigModel.CostType.ITEM)
                ? cost.item()
                : null;
    }

    @Nullable
    public static Identifier getDiscoverItem() {
        var discoverStr = FabricWaystones.CONFIG.requiredDiscoveryItem();

        if (discoverStr.isBlank() || discoverStr.equals("none")) return null;

        return Identifier.tryParse(discoverStr);
    }

    public static boolean isSubSequence(String mainString, String searchString) {
        int j = 0;
        for (int i = 0; i < mainString.length() && j < searchString.length(); ++i) {
            if (mainString.charAt(i) == searchString.charAt(j)) ++j;
            if (j == searchString.length()) return true;
        }
        return false;
    }

    public static Text formatWaystoneName(String name) {
        if (name.isEmpty()) return Text.empty();
        return TagParser.QUICK_TEXT_WITH_STF_SAFE.parseText(name, ParserContext.of());
    }
}
