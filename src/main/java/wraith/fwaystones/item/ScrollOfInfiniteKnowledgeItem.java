package wraith.fwaystones.item;

import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import java.util.HashSet;
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
import net.minecraft.world.level.Level;

public class ScrollOfInfiniteKnowledgeItem extends Item {

    public ScrollOfInfiniteKnowledgeItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return InteractionResult.FAIL;
        }
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (String hash : FabricWaystones.WAYSTONE_STORAGE.getAllHashes()) {
            if (!((PlayerEntityMixinAccess) user).fabricWaystones$hasDiscoveredWaystone(hash)) {
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
                    "fwaystones.learned.infinite.multiple",
                    Component.literal(String.valueOf(learned)).withStyle(style ->
                        style.withColor(TextColor.parseColor(Component.translatable("fwaystones.learned.infinite.multiple.arg_color").getString()).getOrThrow())
                    )
                );
            } else {
                text = Component.translatable("fwaystones.learned.infinite.single");
            }
            ((PlayerEntityMixinAccess) user).fabricWaystones$discoverWaystones(toLearn);
            if (!user.isCreative() && FabricWaystones.CONFIG.consume_infinite_knowledge_scroll_on_use()) {
                stack.shrink(1);
            }
        } else {
            text = Component.translatable("fwaystones.learned.infinite.none");
        }
        user.sendSystemMessage(text);

        if (stack.isEmpty()) {
            user.setItemInHand(hand, ItemStack.EMPTY);
        }
        stack = user.getItemInHand(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        tooltip.accept(Component.translatable("fwaystones.scroll.infinite"));
        int count = -1;
        if (FabricWaystones.WAYSTONE_STORAGE != null) {
            count = FabricWaystones.WAYSTONE_STORAGE.getCount();
        }
        if (count != -1) {
            tooltip.accept(Component.translatable(
                "fwaystones.scroll.infinite_tooltip",
                Component.literal(String.valueOf(count)).withStyle(style ->
                    style.withColor(TextColor.parseColor(Component.translatable("fwaystones.scroll.infinite_tooltip.arg_color").getString()).getOrThrow())
                )
            ));
        }
    }


}
