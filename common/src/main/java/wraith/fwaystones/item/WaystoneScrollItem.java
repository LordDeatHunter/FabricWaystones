package wraith.fwaystones.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;

import java.util.HashSet;
import java.util.List;

public class WaystoneScrollItem extends Item {
    public WaystoneScrollItem(Properties properties) {
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
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return InteractionResultHolder.fail(stack);
        }
        ListTag list = tag.getList(Waystones.MOD_ID, Tag.TAG_STRING);
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (int i = 0; i < list.size(); ++i) {
            String hash = list.getString(i);
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) player).hasDiscoveredWaystone(hash)) {
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
                        "fwaystones.learned.multiple",
                        Component.literal(String.valueOf(learned)).withStyle(style ->
                                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.learned.multiple.arg_color").getString()))
                        )
                );
            } else {
                text = Component.translatable("fwaystones.learned.single");
            }
            ((PlayerEntityMixinAccess) player).discoverWaystones(toLearn);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        } else {
            text = Component.translatable("fwaystones.learned.none");
            stack.setTag(null);
        }
        player.displayClientMessage(text, false);

        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        stack = player.getItemInHand(hand);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof WaystoneBlock && context.getPlayer() != null) {
            var discovered = ((PlayerEntityMixinAccess) context.getPlayer()).getDiscoveredWaystones();

            ItemStack stack = context.getItemInHand();

            if (discovered.isEmpty()) {
                return InteractionResult.FAIL;
            }
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (String hash : discovered) {
                list.add(StringTag.valueOf(hash));
            }
            tag.put(Waystones.MOD_ID, list);
            stack.setTag(tag);
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return;
        }
        int size = tag.getList(Waystones.MOD_ID, Tag.TAG_STRING).size();
        HashSet<String> waystones = null;
        if (Waystones.WAYSTONE_STORAGE != null) {
            waystones = Waystones.WAYSTONE_STORAGE.getAllHashes();
        }
        if (waystones != null) {
            tooltip.add(Component.translatable(
                    "fwaystones.scroll.tooltip",
                    Component.literal(String.valueOf(size)).withStyle(style ->
                            style.withColor(TextColor.parseColor(Component.translatable("fwaystones.scroll.tooltip.arg_color").getString()))
                    )
            ));
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return "item.fwaystones.empty_scroll";
        }
        return "item.fwaystones.waystone_scroll";
    }
}
