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
import wraith.waystones.util.Config;

import java.util.List;

public class LocalVoidItem extends Item {

    public LocalVoidItem(Settings settings) {
        super(settings);
    }

    protected boolean canTeleport = true;
    protected String translationName = "local_void";

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystone")) {
            return canTeleport ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        }
        if (user.isSneaking()) {
            stack.removeSubNbt("waystone");
        } else if (canTeleport) {
            String hash = tag.getString("waystone");
            if (Waystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    stack.removeSubNbt("waystone");
                } else if (waystone.teleportPlayer(user, Config.getInstance().doLocalVoidsUseCost()) && !user.isCreative() && Config.getInstance().consumeLocalVoid()) {
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

        var hash = getBoundWaystone(stack);
        if (hash != null) name = Waystones.WAYSTONE_STORAGE.getName(hash);
        if (name == null) {
            tooltip.add(new TranslatableText("waystones." + translationName + ".empty_tooltip"));
            return;
        }
        tooltip.add(new TranslatableText(
                "waystones." + translationName + ".tooltip",
                new LiteralText(name).styled(style ->
                        style.withColor(TextColor.parse(new TranslatableText("waystones." + translationName + ".tooltip.arg_color").getString()))
                )
        ));
    }

    public static String getBoundWaystone(ItemStack stack) {
        if (!(stack.getItem() instanceof LocalVoidItem)) {
            return null;
        }
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains("waystone")) {
            return null;
        }
        return tag.getString("waystone");
    }

}
