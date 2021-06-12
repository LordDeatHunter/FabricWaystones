package wraith.waystones.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.ClientStuff;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Waystones;
import wraith.waystones.WaystonesClient;
import wraith.waystones.block.WaystoneBlock;

import java.util.HashSet;
import java.util.List;

public class WaystoneScroll extends Item {

    public WaystoneScroll(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("waystones")) {
            return TypedActionResult.fail(stack);
        }
        ListTag list = tag.getList("waystones", 8);
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (int i = 0; i < list.size(); ++i) {
            String hash = list.getString(i);
            if (Waystones.WAYSTONE_STORAGE != null && Waystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) user).hasDiscoveredWaystone(hash)) {
                toLearn.add(hash);
                ++learned;
            }
        }
        Text text;
        if (learned > 0) {
            text = new TranslatableText("waystones.learned.first").append(" " + learned + " ").append(new TranslatableText("waystones.learned.second")).formatted(Formatting.AQUA);
            ((PlayerEntityMixinAccess)user).discoverWaystones(toLearn);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        } else {
            text = new TranslatableText("waystones.learned.none").formatted(Formatting.AQUA);
            stack.setTag(null);
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
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof WaystoneBlock && context.getPlayer() != null) {
            HashSet<String> discovered = ((PlayerEntityMixinAccess)context.getPlayer()).getDiscoveredWaystones();

            ItemStack stack = context.getStack();

            if (discovered.isEmpty()) {
                return ActionResult.FAIL;
            }
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (String hash : discovered) {
                list.add(StringTag.of(hash));
            }
            tag.put("waystones", list);
            stack.setTag(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("waystones")) {
            return;
        }
        int size = tag.getList("waystones", 8).size();
        HashSet<String> waystones = null;
        if (Waystones.WAYSTONE_STORAGE != null) {
            waystones = Waystones.WAYSTONE_STORAGE.getAllHashes();
        } else if (world != null && world.isClient) {
            waystones = ClientStuff.getWaystoneHashes();
        }
        if (waystones != null) {
            tooltip.add(new TranslatableText("waystones.scroll.tooltip").append(" " + size).formatted(Formatting.GOLD));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("waystones")) {
            return "item.waystones.empty_scroll";
        }
        return "item.waystones.waystone_scroll";
    }
}
