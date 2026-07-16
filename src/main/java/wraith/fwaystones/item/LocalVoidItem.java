package wraith.fwaystones.item;

import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.registry.DataComponentRegistry;
import wraith.fwaystones.util.TeleportSources;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class LocalVoidItem extends Item {

    protected boolean canTeleport = true;
    protected String translationName = "local_void";

    public LocalVoidItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = user.getItemInHand(hand);
        if (user.isShiftKeyDown()) {
            stack.remove(DataComponentRegistry.BOUND_WAYSTONE);
            return InteractionResult.PASS;
        } else if (canTeleport) {
            String hash = stack.get(DataComponentRegistry.BOUND_WAYSTONE);
            if (FabricWaystones.WAYSTONE_STORAGE != null) {
                WaystoneBlockEntity waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    stack.remove(DataComponentRegistry.BOUND_WAYSTONE);
                } else if (waystone.teleportPlayer(user, !FabricWaystones.CONFIG.free_local_void_teleport(), TeleportSources.LOCAL_VOID) && !user.isCreative() && FabricWaystones.CONFIG.consume_local_void_on_use()) {
                    stack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        WaystoneBlockEntity entity = WaystoneBlock.getEntity(world, context.getClickedPos());
        if (entity != null && context.getPlayer() != null) {
            ItemStack stack = context.getItemInHand();

            stack.set(DataComponentRegistry.BOUND_WAYSTONE, entity.getHash());
            context.getPlayer().setItemInHand(context.getHand(), stack);

            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        } else {
            return super.useOn(context);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        String name = null;

        var hash = stack.get(DataComponentRegistry.BOUND_WAYSTONE);
        if (hash != null) name = FabricWaystones.WAYSTONE_STORAGE.getName(hash);
        if (name == null) {
            tooltip.accept(Component.translatable("fwaystones." + translationName + ".empty_tooltip"));
            return;
        }
        tooltip.accept(Component.translatable(
            "fwaystones." + translationName + ".tooltip",
            Component.literal(name).withStyle(style ->
                style.withColor(TextColor.parseColor(Component.translatable("fwaystones." + translationName + ".tooltip.arg_color").getString()).getOrThrow())
            )
        ));
    }

}


