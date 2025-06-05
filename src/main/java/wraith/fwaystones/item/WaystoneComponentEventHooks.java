package wraith.fwaystones.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.block.WaystoneBlock;
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

                if (stack.isIn(FabricWaystones.LOCAL_VOID_ITEM)) {
                    if (world.isClient()) return TypedActionResult.success(stack);

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

                        if (waystone == null) {
                            stack.remove(WaystoneDataComponents.HASH_TARGET);
                        } else if (waystone.teleportPlayer(user, !FabricWaystones.CONFIG.free_local_void_teleport(), TeleportSources.LOCAL_VOID) && !user.isCreative() && FabricWaystones.CONFIG.consume_local_void_on_use()) {
                            stack.decrement(1);
                            return TypedActionResult.consume(stack);
                        }
                    }

                    return TypedActionResult.fail(stack);
                } else if(stack.contains(WaystoneDataComponents.HAS_INFINITE_KNOWLEDGE)) {
                    if (world.isClient) return TypedActionResult.success(stack);

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
                            text = Text.translatable(
                                    "fwaystones.learned.infinite.multiple",
                                    Text.literal(String.valueOf(learned)).styled(style ->
                                            style.withColor(TextColor.parse(Text.translatable("fwaystones.learned.infinite.multiple.arg_color").getString()).getOrThrow())
                                    )
                            );
                        } else {
                            text = Text.translatable("fwaystones.learned.infinite.single");
                        }

                        playerData.discoverWaystones(toLearn);

                        if (!user.isCreative() && FabricWaystones.CONFIG.consume_infinite_knowledge_scroll_on_use()) {
                            stack.decrement(1);
                        }
                    } else {
                        text = Text.translatable("fwaystones.learned.infinite.none");
                    }

                    user.sendMessage(text, false);

                    return TypedActionResult.success(stack, world.isClient());
                } else if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
                    if (world.isClient) return TypedActionResult.success(stack);

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
                            text = Text.translatable(
                                    "fwaystones.learned.multiple",
                                    Text.literal(String.valueOf(learned)).styled(style ->
                                            style.withColor(TextColor.parse(Text.translatable("fwaystones.learned.multiple.arg_color").getString()).getOrThrow())
                                    )
                            );
                        } else {
                            text = Text.translatable("fwaystones.learned.single");
                        }
                        WaystonePlayerData.getData(user).discoverWaystones(toLearn);
                        if (!user.isCreative()) {
                            stack.decrement(1);
                        }
                    } else {
                        text = Text.translatable("fwaystones.learned.none");
                    }

                    user.sendMessage(text, false);

                    return TypedActionResult.success(stack, false);
                } else if(stack.contains(WaystoneDataComponents.TELEPORTER)) {
                    var singleUse = stack.get(WaystoneDataComponents.TELEPORTER).oneTimeUse();

                    user.openHandledScreen(createScreenHandlerFactory(singleUse));

                    return TypedActionResult.consume(user.getStackInHand(hand));
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((user, world, hand, hitResult) -> {
            if (!user.isSpectator()) {
                var pos = hitResult.getBlockPos();
                var entity = WaystoneBlock.getEntity(world, pos);
                var stack = user.getStackInHand(hand);

                if (entity != null && stack.isIn(FabricWaystones.LOCAL_VOID_ITEM)) {
                    stack.set(WaystoneDataComponents.HASH_TARGET, new WaystoneHashTarget(entity.getUUID(), null));

                    user.setStackInHand(hand, stack);
                } else if(entity != null && stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
                    var discovered = WaystonePlayerData.getData(user).discoveredWaystones();

                    if (discovered.isEmpty()) return ActionResult.FAIL;

                    var hashes = new ArrayList<>(discovered);

                    stack.set(WaystoneDataComponents.HASH_TARGETS, new WaystoneHashTargets(hashes));

                    user.setStackInHand(hand, stack);
                }
            }

            return ActionResult.PASS;
        });
    }

    public static NamedScreenHandlerFactory createScreenHandlerFactory(boolean singleUse) {
        var title = Text.translatable("container." + FabricWaystones.MOD_ID + (singleUse ? ".abyss_watcher" : ".pocket_wormhole"));

        return new SimpleNamedScreenHandlerFactory((i, inv, player) -> new PortableWaystoneScreenHandler(i, inv), title);
    }

    @Nullable
    public static ItemStack getStack(PlayerEntity player) {
        ItemStack stack = null;

        for (Hand hand : Hand.values()) {
            var currentStack = player.getStackInHand(hand);
            if (currentStack.getItem() != WaystoneItems.get("void_totem")) continue;
            stack = currentStack.copy();
            currentStack.decrement(1);
            break;
        }

        return stack;
    }

    public static String getLocalVoidName(@Nullable ItemStack stack) {
        return (stack != null && stack.getItem() == WaystoneItems.get("void_totem")) ? "void_totem" : "local_void";
    }

    public static String getTranslationKey(ItemStack stack) {
        if (stack.getItem() == WaystoneItems.get("waystone_scroll")) {
            var targets = stack.get(WaystoneDataComponents.HASH_TARGETS);

            if (targets == null || targets.ids().isEmpty()) return "item.fwaystones.empty_scroll";
        }

        return null;
    }
}
