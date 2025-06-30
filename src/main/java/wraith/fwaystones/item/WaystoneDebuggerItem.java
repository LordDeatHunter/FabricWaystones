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
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.util.Utils;

import java.util.List;

public class WaystoneDebuggerItem extends Item {

    private static final int TOOLTIP_MESSAGE = Utils.getRandomIntInRange(1, 4);

    public WaystoneDebuggerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var player = context.getPlayer();

        if (player == null) return ActionResult.FAIL;

        var world = context.getWorld();
        var pos = context.getBlockPos();

        var waystone = WaystoneBlock.getEntity(world, pos);

        if (waystone == null) return ActionResult.FAIL;

        var hash = waystone.position();
        var data = waystone.getData();

        if (data != null) {
            var message = Text.literal("");

            if (data instanceof NetworkedWaystoneData networkedData) {
                var owner = networkedData.ownerID();
                var ownerName = networkedData.ownerID();

                message.append("§6[§eNAME§6]§e=§3").append(networkedData.name());
                message.append("\n§6[§eGLOBAL§6]§e=§3" + networkedData.global());

                message.append("\n§6[§eOWNER-UUID§6]§e=§3" + (owner != null ? owner : "NONE"));
                message.append("\n§6[§eOWNER-NAME§6]§e=§3" + (ownerName != null ? ownerName : "NONE"));
            }

            message.append("\n§6[§eHASH§6]§e=§3" + hash.getHexHash());
            message.append("\n§6[§eCOLOR§6]§e=§3" + data.color());

            player.sendMessage(message, false);
        }

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
        tooltip.add(TextUtils.translationWithArg("debug.debugger_tooltip_base", TextUtils.translationWithArg("debug.debugger_tooltip" + TOOLTIP_MESSAGE)));
    }

}
