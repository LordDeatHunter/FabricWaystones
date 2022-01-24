package wraith.waystones.item;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;

import java.util.HashSet;
import java.util.List;

public class WaystoneScrollItem extends Item implements PolymerItem {

    public WaystoneScrollItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystones")) {
            return TypedActionResult.fail(stack);
        }
        NbtList list = tag.getList("waystones", 8);
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (int i = 0; i < list.size(); ++i) {
            String hash = list.getString(i);
            if (Waystones.WAYSTONE_STORAGE != null && Waystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) user).hasDiscoveredWaystone(hash)) {
                var waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone.getOwner() == null) {
                    waystone.setOwner(user);
                }
                toLearn.add(hash);
                ++learned;
            }
        }
        Text text;
        if (learned > 0) {
            if (learned > 1) {
                text = new TranslatableText(
                        "waystones.learned.multiple",
                        new LiteralText(String.valueOf(learned)).styled(style ->
                                style.withColor(TextColor.parse(new TranslatableText("waystones.learned.multiple.arg_color").getString()))
                        )
                );
            } else {
                text = new TranslatableText("waystones.learned.single");
            }
            ((PlayerEntityMixinAccess) user).discoverWaystones(toLearn);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        } else {
            text = new TranslatableText("waystones.learned.none");
            stack.setNbt(null);
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
            HashSet<String> discovered = ((PlayerEntityMixinAccess) context.getPlayer()).getDiscoveredWaystones();

            ItemStack stack = context.getStack();

            if (discovered.isEmpty()) {
                return ActionResult.FAIL;
            }
            NbtCompound tag = new NbtCompound();
            NbtList list = new NbtList();
            for (String hash : discovered) {
                list.add(NbtString.of(hash));
            }
            tag.put("waystones", list);
            stack.setNbt(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystones")) {
            return;
        }
        int size = tag.getList("waystones", 8).size();
        HashSet<String> waystones = null;
        if (Waystones.WAYSTONE_STORAGE != null) {
            waystones = Waystones.WAYSTONE_STORAGE.getAllHashes();
        }
        if (waystones != null) {
            tooltip.add(new TranslatableText(
                    "waystones.scroll.tooltip",
                    new LiteralText(String.valueOf(size)).styled(style ->
                            style.withColor(TextColor.parse(new TranslatableText("waystones.scroll.tooltip.arg_color").getString()))
                    )
            ));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystones")) {
            return "item.waystones.empty_scroll";
        }
        return "item.waystones.waystone_scroll";
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.FLOWER_BANNER_PATTERN;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, player);
        stack.addEnchantment(Enchantments.LURE, 2);
        return stack;
    }
}
