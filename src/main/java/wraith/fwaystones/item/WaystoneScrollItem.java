package wraith.fwaystones.item;

import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.registry.DataComponentRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class WaystoneScrollItem extends Item {

    public WaystoneScrollItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return InteractionResult.FAIL;
        }
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        if (waystones == null || waystones.isEmpty()) {
            return InteractionResult.FAIL;
        }
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (String hash : waystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) user).fabricWaystones$hasDiscoveredWaystone(hash)) {
                var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone != null && waystone.getOwner() == null) {
                    waystone.setOwner(user);
                }
                toLearn.add(hash);
                ++learned;
            }
        }
        Component text;
        if (learned > 0) {
            if (learned > 1) {
                text = Component.translatable(
                    "fwaystones.learned.multiple",
                    Component.literal(String.valueOf(learned)).withStyle(style ->
                        style.withColor(TextColor.parseColor(Component.translatable("fwaystones.learned.multiple.arg_color").getString()).getOrThrow())
                    )
                );
            } else {
                text = Component.translatable("fwaystones.learned.single");
            }
            ((PlayerEntityMixinAccess) user).fabricWaystones$discoverWaystones(toLearn);
            if (!user.isCreative()) {
                stack.shrink(1);
            }
        } else {
            text = Component.translatable("fwaystones.learned.none");
            stack.set(DataComponentRegistry.WAYSTONES, null);
        }
        user.displayClientMessage(text, false);

        if (stack.isEmpty()) {
            stack = ItemStack.EMPTY;
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof WaystoneBlock && context.getPlayer() != null) {
            var discovered = ((PlayerEntityMixinAccess) context.getPlayer()).fabricWaystones$getDiscoveredWaystones();

            ItemStack stack = context.getItemInHand();

            if (discovered.isEmpty()) {
                return InteractionResult.FAIL;
            }
            List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
            if (waystones == null) {
                waystones = discovered.stream().toList();
            } else {
                // Create a set to avoid duplicates
                var set = new HashSet<>(waystones);
                set.addAll(discovered);
                waystones = List.copyOf(set);
            }
            stack.set(DataComponentRegistry.WAYSTONES, waystones);

            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        if (waystones == null || waystones.isEmpty()) {
            return;
        }
        tooltip.accept(Component.translatable(
            "fwaystones.scroll.tooltip",
            Component.literal(String.valueOf(waystones.size())).withStyle(style ->
                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.scroll.tooltip.arg_color").getString()).getOrThrow())
            )
        ));
    }

    @Override
    public Component getName(ItemStack stack) {
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        return waystones == null || waystones.isEmpty() ? Component.translatable("item.fwaystones.empty_scroll") : Component.translatable("item.fwaystones.waystone_scroll");
    }
}
