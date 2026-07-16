package wraith.fwaystones.item;

import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.Utils;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WaystoneDebuggerItem extends Item {

    private static final int TOOLTIP_MESSAGE = Utils.getRandomIntInRange(1, 4);

    public WaystoneDebuggerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var world = context.getLevel();
        var pos = context.getClickedPos();
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();
        var player = context.getPlayer();
        if (!(block instanceof WaystoneBlock) || player == null) {
            return InteractionResult.FAIL;
        }
        var entityPos = blockState.getValue(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (!(world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone)) {
            return InteractionResult.FAIL;
        }
        var owner = waystone.getOwner();
        var ownerName = waystone.getOwnerName();

        var message = Component.literal("");
        message.append("§6[§eNAME§6]§e=§3" + waystone.getWaystoneName());
        message.append("\n§6[§eGLOBAL§6]§e=§3" + waystone.isGlobal());
        message.append("\n§6[§eHASH§6]§e=§3" + waystone.getHexHash());
        message.append("\n§6[§eCOLOR§6]§e=§3" + waystone.getColor());
        if (owner != null && ownerName != null) {
            message.append("\n§6[§eOWNER-UUID§6]§e=§3" + waystone.getOwner());
            message.append("\n§6[§eOWNER-NAME§6]§e=§3" + waystone.getOwnerName());
        } else {
            message.append("\n§6[§eNO-OWNER§6]");
        }
        player.displayClientMessage(message, false);

        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        var playerAccess = (PlayerEntityMixinAccess) player;

        var message = Component.literal("");
        message.append("§6[§eNAME§6]§e=§3" + player.getName().getString());
        message.append("\n§6[§eKNOWN-WAYSTONES§6]§e=§3" + playerAccess.fabricWaystones$getDiscoveredCount());
        message.append("\n§6[§eCOOLDOWN§6]§e=§3" + playerAccess.fabricWaystones$getTeleportCooldown());
        user.displayClientMessage(message, false);

        return super.interactLivingEntity(stack, user, entity, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        tooltip.accept(Component.translatable("fwaystones.debug.debugger_tooltip" + TOOLTIP_MESSAGE));
    }

}
