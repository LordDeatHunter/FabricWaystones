package wraith.fwaystones.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.registry.DataComponentRegistry;
import wraith.fwaystones.util.TeleportSources;
import java.util.List;

public class LocalVoidItem extends Item {

    protected boolean canTeleport = true;
    protected String translationName = "local_void";

    public LocalVoidItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            stack.remove(DataComponentRegistry.BOUND_WAYSTONE);
            return ActionResult.PASS;
        } else if (canTeleport) {
            String hash = stack.get(DataComponentRegistry.BOUND_WAYSTONE);
            if (FabricWaystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    stack.remove(DataComponentRegistry.BOUND_WAYSTONE);
                } else if (waystone.teleportPlayer(user, !FabricWaystones.CONFIG.free_local_void_teleport(), TeleportSources.LOCAL_VOID) && !user.isCreative() && FabricWaystones.CONFIG.consume_local_void_on_use()) {
                    stack.decrement(1);
                    return ActionResult.CONSUME;
                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        WaystoneBlockEntity entity = WaystoneBlock.getEntity(world, context.getBlockPos());
        if (entity != null && context.getPlayer() != null) {
            ItemStack stack = context.getStack();

            stack.set(DataComponentRegistry.BOUND_WAYSTONE, entity.getHash());
            context.getPlayer().setStackInHand(context.getHand(), stack);

            return ActionResult.SUCCESS.withNewHandStack(stack);
        } else {
            return super.useOnBlock(context);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        String name = null;

        var hash = stack.get(DataComponentRegistry.BOUND_WAYSTONE);
        if (hash != null) name = FabricWaystones.WAYSTONE_STORAGE.getName(hash);
        if (name == null) {
            tooltip.add(Text.translatable("fwaystones." + translationName + ".empty_tooltip"));
            return;
        }
        tooltip.add(Text.translatable(
            "fwaystones." + translationName + ".tooltip",
            Text.literal(name).styled(style ->
                style.withColor(TextColor.parse(Text.translatable("fwaystones." + translationName + ".tooltip.arg_color").getString()).getOrThrow())
            )
        ));
    }

}


