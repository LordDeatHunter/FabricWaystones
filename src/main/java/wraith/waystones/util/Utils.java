package wraith.waystones.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;
import wraith.waystones.Waystones;
import wraith.waystones.access.StructurePiecesListAccess;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.mixin.SinglePoolElementAccessor;
import wraith.waystones.mixin.StructurePoolAccessor;
import wraith.waystones.mixin.StructureStartAccessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
        var poolGetter = server.getRegistryManager()
                .get(Registry.STRUCTURE_POOL_KEY)
                .getEntries().stream()
                .filter(p -> p.getKey().getValue().equals(village))
                .findFirst();
        if (poolGetter.isEmpty()) {
            Waystones.LOGGER.error("Cannot add to " + village + " as it cannot be found!");
            return;
        }
        var pool = poolGetter.get().getValue();

        var pieceList = ((StructurePoolAccessor) pool).getElements();
        var piece = StructurePoolElement.ofSingle(waystone.toString()).apply(StructurePool.Projection.RIGID);

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

    public static boolean canTeleport(PlayerEntity player, String hash) {
        String cost = Config.getInstance().teleportType();
        int amount = Config.getInstance().teleportCost();
        if (player.isCreative()) {
            return true;
        }
        switch (cost) {
            case "hp":
            case "health":
                if (player.getHealth() + player.getAbsorptionAmount() <= amount) {
                    return false;
                }
                player.damage(DamageSource.OUT_OF_WORLD, amount);
                return true;
            case "hunger":
            case "saturation":
                var hungerManager = player.getHungerManager();
                var hungerAndExhaustion = hungerManager.getFoodLevel() + hungerManager.getSaturationLevel();
                if (hungerAndExhaustion <= 10 || hungerAndExhaustion + hungerManager.getExhaustion() / 4F <= amount) {
                    return false;
                }
                hungerManager.addExhaustion(4 * amount);
                return true;
            case "xp":
            case "experience":
                long total = determineLevelXP(player);
                if (total < amount) {
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
                if (!containsItem(player.getInventory(), item, amount)) {
                    return false;
                }
                removeItem(player.getInventory(), Registry.ITEM.get(itemId), amount);

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
                return true;
        }

    }

    private static boolean containsItem(PlayerInventory inventory, Item item, int maxAmount) {
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

    private static void removeItem(PlayerInventory inventory, Item item, int totalAmount) {
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

    public static StructureAccessor populateNoise(StructureAccessor accessor, Chunk chunk) {
        if (!Config.getInstance().generateInVillages()) {
            return accessor;
        }
        ChunkPos chunkPos = chunk.getPos();
        for (int i = 0; i < StructureFeature.LAND_MODIFYING_STRUCTURES.size(); ++i) {
            var structureFeature = StructureFeature.LAND_MODIFYING_STRUCTURES.get(i);
            var waystones = new AtomicInteger(0);
            accessor.getStructureStarts(ChunkSectionPos.from(chunkPos, 0), structureFeature).forEach((structures) -> {
                var oldStructurePieces = new ArrayList<>(structures.getChildren());
                ArrayList<Integer> toRemove = new ArrayList<>();
                for (int j = 0; j < oldStructurePieces.size(); ++j) {
                    StructurePiece structure = oldStructurePieces.get(j);
                    if (structure instanceof PoolStructurePiece poolStructurePiece &&
                            ((PoolStructurePiece) structure).getPoolElement() instanceof SinglePoolElement &&
                            WaystonesWorldgen.WAYSTONE_STRUCTURES.contains(((SinglePoolElementAccessor) (poolStructurePiece).getPoolElement()).getLocation().left().get()) &&
                            waystones.getAndIncrement() > 0) {
                        toRemove.add(j);
                    }
                }
                toRemove.sort(Collections.reverseOrder());
                for (int remove : toRemove) {
                    oldStructurePieces.remove(remove);
                }
                ((StructurePiecesListAccess) (Object) (((StructureStartAccessor) (Object) structures).getChildren())).setPieces(oldStructurePieces);
            });
        }
        return accessor;
    }

}
