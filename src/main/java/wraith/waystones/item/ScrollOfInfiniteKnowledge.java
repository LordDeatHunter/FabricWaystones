package wraith.waystones.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.ClientStuff;
import wraith.waystones.Config;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Waystones;

import java.util.HashSet;
import java.util.List;

public class ScrollOfInfiniteKnowledge extends Item {

    public ScrollOfInfiniteKnowledge(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (Waystones.WAYSTONE_STORAGE == null) {
            return TypedActionResult.fail(stack);
        }
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (String waystone : Waystones.WAYSTONE_STORAGE.getAllHashes()) {
            if (!((PlayerEntityMixinAccess) user).hasDiscoveredWaystone(waystone)) {
                toLearn.add(waystone);
                ++learned;
            }
        }
        Text text;
        if (learned > 0) {
            text = new TranslatableText("waystones.learned.infinite.first").append(" " + learned + " ").append(new TranslatableText("waystones.learned.infinite.second")).formatted(Formatting.AQUA);
            ((PlayerEntityMixinAccess)user).discoverWaystones(toLearn);
            if (!user.isCreative() && Config.getInstance().consumeInfiniteScroll()) {
                stack.decrement(1);
            }
        } else {
            text = new TranslatableText("waystones.learned.infinite.none").formatted(Formatting.AQUA);
        }
        if (!world.isClient) {
            user.sendMessage(text, false);
        }

        if (stack.isEmpty()) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        stack = user.getStackInHand(hand);
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(new TranslatableText("waystones.scroll.infinite").formatted(Formatting.GOLD));
        int count = -1;
        if (Waystones.WAYSTONE_STORAGE != null) {
            count = Waystones.WAYSTONE_STORAGE.getCount();
        } else if (world != null && world.isClient) {
            count = ClientStuff.getWaystoneCount();
        }
        if (count != -1) {
            tooltip.add(new TranslatableText("waystones.scroll.infinite_tooltip").append(" " + count).formatted(Formatting.GOLD));
        }
    }


}
