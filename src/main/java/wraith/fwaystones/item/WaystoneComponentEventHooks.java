package wraith.fwaystones.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.ExtendedStackReference;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.item.components.WaystoneHashTarget;
import wraith.fwaystones.item.components.WaystoneHashTargets;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.registry.WaystoneItems;
import wraith.fwaystones.client.screen.PortableWaystoneScreenHandler;
import wraith.fwaystones.util.TeleportSources;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class WaystoneComponentEventHooks {

    public static void init() {
        UseItemCallback.EVENT.register((user, world, hand) -> {
            if (!user.isSpectator()) {
                var stack = user.getStackInHand(hand);

                TypedActionResult<ItemStack> result = null;

                if (stack.isIn(FabricWaystones.LOCAL_VOID_ITEM)) {
                    result = useLocalVoid(world, user, stack);
                } else if (stack.contains(WaystoneDataComponents.TELEPORTER)) {
                    result = useTeleporter(user, stack);
                } else if(stack.contains(WaystoneDataComponents.HAS_INFINITE_KNOWLEDGE)) {
                    if (!world.isClient) {
                        var storage = WaystoneDataStorage.getStorage(world);
                        var playerData = WaystonePlayerData.getData(user);

                        int learned = 0;

                        var toLearn = new HashSet<UUID>();

                        for (var uuid : storage.getAllIds()) {
                            if (!playerData.hasDiscoverdWaystone(uuid)) {
                                var data = storage.getData(uuid);

                                if (data != null && data.owner() == null) {
                                    data.setOwner(user);
                                }

                                toLearn.add(uuid);
                                ++learned;
                            }
                        }

                        Text text;
                        if (learned > 0) {
                            if (learned > 1) {
                                text = TextUtils.translationWithArg(
                                        "learned.infinite.multiple",
                                        String.valueOf(learned)
                                );
                            } else {
                                text = TextUtils.translation("learned.infinite.single");
                            }

                            playerData.discoverWaystones(toLearn);

                            if (!user.isCreative() && FabricWaystones.CONFIG.shouldConsumeInfiniteKnowledgeScroll()) {
                                stack.decrement(1);
                            }
                        } else {
                            text = TextUtils.translation("learned.infinite.none");
                        }

                        user.sendMessage(text, false);
                    }

                    result = TypedActionResult.success(stack, world.isClient());
                } else if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
                    if (!world.isClient) {
                        var targets = WaystoneHashTargets.get(stack, user.getWorld());
                        var playerData = WaystonePlayerData.getData(user);

                        int learned = 0;
                        var toLearn = new HashSet<UUID>();

                        var storage = WaystoneDataStorage.getStorage(user);
                        for (var uuid : targets.ids()) {
                            if (!playerData.hasDiscoverdWaystone(uuid)) {
                                var data = storage.getData(uuid);

                                if (data != null && data.owner() == null) {
                                    data.setOwner(user);
                                }

                                toLearn.add(uuid);
                                ++learned;
                            }
                        }

                        Text text;
                        if (learned > 0) {
                            if (learned > 1) {
                                text = TextUtils.translationWithArg(
                                        "learned.multiple",
                                        String.valueOf(learned)
                                );
                            } else {
                                text = TextUtils.translation("learned.single");
                            }
                            WaystonePlayerData.getData(user).discoverWaystones(toLearn);
                            if (!user.isCreative()) {
                                stack.decrement(1);
                            }
                        } else {
                            text = TextUtils.translation("learned.none");
                        }

                        user.sendMessage(text, false);
                    }

                    result = TypedActionResult.success(stack, false);;
                }

                if (result != null) return result;
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((user, world, hand, hitResult) -> {
            if (!user.isSpectator()) {
                var pos = hitResult.getBlockPos();
                var entity = WaystoneBlock.getEntity(world, pos);
                var stack = user.getStackInHand(hand);

                if (entity != null && stack.isIn(FabricWaystones.LOCAL_VOID_ITEM)) {
                    if (!world.isClient) {
                        stack.set(WaystoneDataComponents.HASH_TARGET, new WaystoneHashTarget(entity.getUUID(), null));

                        user.setStackInHand(hand, stack);
                    }

                    return ActionResult.SUCCESS;
                } else if(entity != null && stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
                    if (!world.isClient) {
                        var discovered = WaystonePlayerData.getData(user).discoveredWaystones();

                        if (discovered.isEmpty()) return ActionResult.FAIL;

                        var hashes = new ArrayList<>(discovered);

                        stack.set(WaystoneDataComponents.HASH_TARGETS, new WaystoneHashTargets(hashes));

                        user.setStackInHand(hand, stack);
                    }

                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });

        WaystoneInteractionEvents.LOCATE_EQUIPMENT.register((player, predicate) -> {
            for (var hand : Hand.values()) {
                var currentStack = player.getStackInHand(hand);

                if (predicate.test(currentStack)) {
                    return ExtendedStackReference.of(() -> player.getStackInHand(hand), stack -> player.setStackInHand(hand, stack), stack -> {
                        player.sendEquipmentBreakStatus(stack.getItem(), hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    });
                }
            }

            return null;
        });
    }

    public static TypedActionResult<ItemStack> useTeleporter(PlayerEntity user, ItemStack stack) {
        var data = stack.get(WaystoneDataComponents.TELEPORTER);

        if (data == null) return TypedActionResult.pass(ItemStack.EMPTY);

        var singleUse = data.oneTimeUse();

        var title = Text.translatable("container." + FabricWaystones.MOD_ID + (singleUse ? ".abyss_watcher" : ".pocket_wormhole"));

        user.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, inv, player) -> new PortableWaystoneScreenHandler(i, inv), title));

        return TypedActionResult.consume(stack);
    }

    public static TypedActionResult<ItemStack> useLocalVoid(World world, PlayerEntity user, ItemStack stack) {
        var target = WaystoneHashTarget.get(stack, world);

        var canTeleport = stack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEM);

        if (target == null) return canTeleport ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);

        var flag = target.allowTeleportOnUse();

        if (flag != null) canTeleport = flag;

        if (user.isSneaking()) {
            stack.remove(WaystoneDataComponents.HASH_TARGET);

            return TypedActionResult.pass(stack);
        }

        if (canTeleport) {
            var storage = WaystoneDataStorage.getStorage(world);
            var waystone = storage.getEntity(target.uuid());

            if (!world.isClient) {
                if (waystone == null) {
                    stack.remove(WaystoneDataComponents.HASH_TARGET);
                } else if (waystone.teleportPlayer(user, !FabricWaystones.CONFIG.shouldLocalVoidTeleportBeFree(), TeleportSources.LOCAL_VOID) && !user.isCreative() && FabricWaystones.CONFIG.shouldConsumeLocalVoid()) {
                    stack.decrement(1);
                }
            }

            return TypedActionResult.consume(stack);
        }

        return TypedActionResult.fail(stack);
    }

    @Nullable
    public static ItemStack getVoidTotem(PlayerEntity player) {
        var item = WaystoneItems.VOID_TOTEM;

        var ref = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(player, currentStack -> currentStack.isOf(item));

        if (ref == null) return null;

        var currentStack = ref.get();

        ItemStack originalStack = currentStack.copy();

        currentStack.decrement(1);

        ref.set(currentStack);

        return originalStack;
    }

    public static String getLocalVoidName(@Nullable ItemStack stack) {
        return (stack != null && stack.getItem() == WaystoneItems.VOID_TOTEM) ? "void_totem" : "local_void";
    }

    public static String getTranslationKey(ItemStack stack) {
        if (stack.getItem() == WaystoneItems.WAYSTONE_SCROLL) {
            var targets = stack.get(WaystoneDataComponents.HASH_TARGETS);

            if (targets == null || targets.ids().isEmpty()) return "item.fwaystones.empty_scroll";
        }

        return null;
    }
}
