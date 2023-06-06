package wraith.fwaystones.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

import java.util.HashSet;
import java.util.List;

public class ScrollOfInfiniteKnowledgeItem extends Item {
    public ScrollOfInfiniteKnowledgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (Waystones.WAYSTONE_STORAGE == null) {
            return InteractionResultHolder.fail(stack);
        }
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (String hash : Waystones.WAYSTONE_STORAGE.getAllHashes()) {
            if (!((PlayerEntityMixinAccess) player).hasDiscoveredWaystone(hash)) {
                var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone != null && waystone.getOwner() == null) {
                    waystone.setOwner(player);
                }
                toLearn.add(hash);
                ++learned;
            }
        }
        Component text;
        if (learned > 0) {
            if (learned > 1) {
                text = Component.translatable(
                        "fwaystones.learned.infinite.multiple",
                        Component.literal(String.valueOf(learned)).withStyle(style ->
                                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.learned.infinite.multiple.arg_color").getString()))
                        )
                );
            } else {
                text = Component.translatable("fwaystones.learned.infinite.single");
            }
            ((PlayerEntityMixinAccess) player).discoverWaystones(toLearn);
            if (!player.isCreative() && Waystones.CONFIG.consume_infinite_knowledge_scroll_on_use) {
                stack.shrink(1);
            }
        } else {
            text = Component.translatable("fwaystones.learned.infinite.none");
        }
        player.displayClientMessage(text, false);

        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        stack = player.getItemInHand(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        tooltip.add(Component.translatable("fwaystones.scroll.infinite"));
        int count = -1;
        if (Waystones.WAYSTONE_STORAGE != null) {
            count = Waystones.WAYSTONE_STORAGE.getCount();
        }
        if (count != -1) {
            tooltip.add(Component.translatable(
                    "fwaystones.scroll.infinite_tooltip",
                    Component.literal(String.valueOf(count)).withStyle(style ->
                            style.withColor(TextColor.parseColor(Component.translatable("fwaystones.scroll.infinite_tooltip.arg_color").getString()))
                    )
            ));
        }
    }
}
