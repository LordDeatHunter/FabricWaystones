package wraith.fwaystones.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.Utils;

import java.util.List;

public class WaystoneDebuggerItem extends Item {
    private static final int TOOLTIP_MESSAGE = Utils.getRandomIntInRange(1, 4);
    public WaystoneDebuggerItem(Properties properties) {
        super(properties);
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
        message.append(Component.translatable("fwaystones.debug.msg.splitter"));
        message.append("\n"+Component.translatable("fwaystones.debug.msg.name").getString()+waystone.getWaystoneName());
        message.append("\n"+Component.translatable("fwaystones.debug.msg.global").getString()+waystone.isGlobal());
        message.append("\n"+Component.translatable("fwaystones.debug.msg.hash").getString()+waystone.getHexHash());
        message.append("\n"+Component.translatable("fwaystones.debug.msg.color").getString()+waystone.getColor());
        if (owner != null && ownerName != null) {
            message.append("\n"+Component.translatable("fwaystones.debug.msg.owner_uuid").getString()+waystone.getOwner());
            message.append("\n"+Component.translatable("fwaystones.debug.msg.owner_name").getString()+waystone.getOwnerName());
        } else {
            message.append("\n"+Component.translatable("fwaystones.debug.msg.no_owner").getString());
        }
        player.displayClientMessage(message, false);

        return super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof ServerPlayer player2)){
            return InteractionResult.PASS;
        }
        var playerAccess = (PlayerEntityMixinAccess) player2;

        var message = Component.literal("");
        message.append(Component.translatable("fwaystones.debug.msg.splitter"));
        message.append("\n"+Component.translatable("fwaystones.debug.msg.name").getString()+player2.getName().getString());
        message.append("\n"+Component.translatable("fwaystones.debug.msg.known_waystones").getString()+playerAccess.getDiscoveredCount());
        message.append("\n"+Component.translatable("fwaystones.debug.msg.cooldown").getString()+playerAccess.getTeleportCooldown());
        player.displayClientMessage(message, false);

        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        tooltip.add(Component.translatable("fwaystones.debug.debugger_tooltip" + TOOLTIP_MESSAGE));
    }
}
