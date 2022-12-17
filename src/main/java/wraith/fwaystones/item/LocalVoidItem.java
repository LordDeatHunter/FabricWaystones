package wraith.fwaystones.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;

import java.util.List;

public class LocalVoidItem extends Item {

    protected boolean canTeleport = true;
    protected String translationName = "local_void";

    public LocalVoidItem(Settings settings) {
        super(settings);
    }

    public static String getBoundWaystone(ItemStack stack) {
        if (!(stack.getItem() instanceof LocalVoidItem)) {
            return null;
        }
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return null;
        }
        return tag.getString(FabricWaystones.MOD_ID);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return canTeleport ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        }
        if (user.isSneaking()) {
            stack.removeSubNbt(FabricWaystones.MOD_ID);
        } else if (canTeleport) {
            String hash = tag.getString(FabricWaystones.MOD_ID);
            if (FabricWaystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    stack.removeSubNbt(FabricWaystones.MOD_ID);
                } else if (waystone.teleportPlayer(user, !FabricWaystones.CONFIG.free_local_void_teleport()) && !user.isCreative() && FabricWaystones.CONFIG.consume_local_void_on_use()) {
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
            tag.putString(FabricWaystones.MOD_ID, entity.getHash());
            stack.setNbt(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        String name = null;

        var hash = getBoundWaystone(stack);
        if (hash != null) name = FabricWaystones.WAYSTONE_STORAGE.getName(hash);
        if (name == null) {
            tooltip.add(Text.translatable("fwaystones." + translationName + ".empty_tooltip"));
            return;
        }
        tooltip.add(Text.translatable(
            "fwaystones." + translationName + ".tooltip",
            Text.literal(name).styled(style ->
                style.withColor(TextColor.parse(Text.translatable("fwaystones." + translationName + ".tooltip.arg_color").getString()))
            )
        ));
    }

}
