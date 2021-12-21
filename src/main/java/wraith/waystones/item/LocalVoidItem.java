package wraith.waystones.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.client.WaystonesClient;
import wraith.waystones.util.Config;

import java.util.List;

public class LocalVoidItem extends Item {

    public LocalVoidItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystone")) {
            return TypedActionResult.fail(stack);
        }
        if (user.isSneaking()) {
            stack.removeSubNbt("waystone");
        } else {
            String hash = tag.getString("waystone");
            if (Waystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone != null && waystone.teleportPlayer(user, !Config.getInstance().areLocalVoidsFree()) && !user.isCreative() && Config.getInstance().consumeLocalVoid()) {
                    stack.decrement(1);
                }
            }
        }
        if (stack.isEmpty()) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        WaystoneBlockEntity entity = WaystoneBlock.getEntity(world, context.getBlockPos());
        if (entity != null) {
            ItemStack stack = context.getStack();
            NbtCompound tag = new NbtCompound();
            tag.putString("waystone", entity.getHash());
            stack.setNbt(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String name = null;
        NbtCompound tag = stack.getNbt();
        boolean invalid = false;
        if (tag == null || !tag.contains("waystone")) {
            invalid = true;
        } else {
            name = WaystonesClient.WAYSTONE_STORAGE.getName(tag.getString("waystone"));
            if (name == null) {
                invalid = true;
            }
        }
        if (invalid) {
            tooltip.add(new TranslatableText("waystones.local_void.empty_tooltip"));
            return;
        }
        tooltip.add(new TranslatableText(
                "waystones.local_void.tooltip",
                new LiteralText(name).styled(style ->
                        style.withColor(TextColor.parse(new TranslatableText("waystones.local_void.tooltip.arg_color").getString()))
                )
        ));
    }

}
