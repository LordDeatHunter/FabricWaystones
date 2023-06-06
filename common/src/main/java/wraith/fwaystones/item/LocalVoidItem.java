package wraith.fwaystones.item;

import net.minecraft.nbt.CompoundTag;
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
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;

import java.util.List;

public class LocalVoidItem extends Item {

    protected boolean canTeleport = true;
    protected String translationName = "local_void";
    public LocalVoidItem(Properties properties) {
        super(properties);
    }
    public static String getBoundWaystone(ItemStack stack) {
        if (!(stack.getItem() instanceof LocalVoidItem)) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return null;
        }
        return tag.getString(Waystones.MOD_ID);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Waystones.MOD_ID)) {
            return canTeleport ? InteractionResultHolder.pass(stack) : InteractionResultHolder.fail(stack);
        }
        if (player.isCrouching()) {
            stack.removeTagKey(Waystones.MOD_ID);
        } else if (canTeleport) {
            String hash = tag.getString(Waystones.MOD_ID);
            if (Waystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    stack.removeTagKey(Waystones.MOD_ID);
                } else if (waystone.teleportPlayer(player, !Waystones.CONFIG.free_local_void_teleport) && !player.isCreative() && Waystones.CONFIG.consume_local_void_on_use) {
                    stack.shrink(1);
                }
            }
        }
        if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        WaystoneBlockEntity entity = WaystoneBlock.getEntity(level, context.getClickedPos());
        if (entity != null) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = new CompoundTag();
            tag.putString(Waystones.MOD_ID, entity.getHash());
            stack.setTag(tag);
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        String name = null;

        var hash = getBoundWaystone(stack);
        if (hash != null) name = Waystones.WAYSTONE_STORAGE.getName(hash);
        if (name == null) {
            tooltip.add(Component.translatable("fwaystones." + translationName + ".empty_tooltip"));
            return;
        }
        tooltip.add(Component.translatable(
                "fwaystones." + translationName + ".tooltip",
                Component.literal(name).withStyle(style ->
                        style.withColor(TextColor.parseColor(Component.translatable("fwaystones." + translationName + ".tooltip.arg_color").getString()))
                )
        ));
    }
}
