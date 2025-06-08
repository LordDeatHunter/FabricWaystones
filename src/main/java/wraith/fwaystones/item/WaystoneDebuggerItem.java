package wraith.fwaystones.item;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.item.components.TooltipUtils;
import wraith.fwaystones.util.Utils;

import java.util.List;

public class WaystoneDebuggerItem extends Item {

    private static final int TOOLTIP_MESSAGE = Utils.getRandomIntInRange(1, 4);

    public WaystoneDebuggerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        var pos = context.getBlockPos();
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();
        var player = context.getPlayer();
        if (!(block instanceof WaystoneBlock) || player == null) {
            return ActionResult.FAIL;
        }
        var entityPos = blockState.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
        if (!(world.getBlockEntity(entityPos) instanceof WaystoneBlockEntity waystone)) {
            return ActionResult.FAIL;
        }
        var hash = waystone.position();
        var data = waystone.getData();

        var owner = data.owner();
        var ownerName = data.owner();

        var message = Text.literal("");
        message.append("§6[§eNAME§6]§e=§3").append(data.name());
        message.append("\n§6[§eGLOBAL§6]§e=§3" + data.global());
        message.append("\n§6[§eHASH§6]§e=§3" + hash.getHexHash());
        message.append("\n§6[§eCOLOR§6]§e=§3" + data.color());
        if (owner != null && ownerName != null) {
            message.append("\n§6[§eOWNER-UUID§6]§e=§3" + owner);
            message.append("\n§6[§eOWNER-NAME§6]§e=§3" + ownerName);
        } else {
            message.append("\n§6[§eNO-OWNER§6]");
        }
        player.sendMessage(message, false);

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof ServerPlayerEntity player)) return ActionResult.PASS;

        var data = WaystonePlayerData.getData(player);

        var message = Text.literal("");
        message.append("§6[§eNAME§6]§e=§3" + player.getName().getString());
        message.append("\n§6[§eKNOWN-WAYSTONES§6]§e=§3" + data.discoveredWaystones().size());
        message.append("\n§6[§eCOOLDOWN§6]§e=§3" + data.teleportCooldown());
        user.sendMessage(message, false);

        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(TooltipUtils.translationWithArg("debug.debugger_tooltip_base", String.valueOf(TOOLTIP_MESSAGE)));
    }

}
