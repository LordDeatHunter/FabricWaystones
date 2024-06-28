package wraith.fwaystones.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

import java.util.HashSet;
import java.util.List;

public class ScrollOfInfiniteKnowledgeItem extends Item {

    public ScrollOfInfiniteKnowledgeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return TypedActionResult.fail(stack);
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
            ((PlayerEntityMixinAccess) user).fabricWaystones$discoverWaystones(toLearn);
            if (!user.isCreative() && FabricWaystones.CONFIG.consume_infinite_knowledge_scroll_on_use()) {
                stack.decrement(1);
            }
        } else {
            text = Text.translatable("fwaystones.learned.infinite.none");
        }
        user.sendMessage(text, false);

        if (stack.isEmpty()) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        stack = user.getStackInHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("fwaystones.scroll.infinite"));
        int count = -1;
        if (FabricWaystones.WAYSTONE_STORAGE != null) {
            count = FabricWaystones.WAYSTONE_STORAGE.getCount();
        }
        if (count != -1) {
            tooltip.add(Text.translatable(
                "fwaystones.scroll.infinite_tooltip",
                Text.literal(String.valueOf(count)).styled(style ->
                    style.withColor(TextColor.parse(Text.translatable("fwaystones.scroll.infinite_tooltip.arg_color").getString()).getOrThrow())
                )
            ));
        }
    }


}
