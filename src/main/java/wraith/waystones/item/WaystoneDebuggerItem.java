package wraith.waystones.item;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.util.Utils;

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
        var owner = waystone.getOwner();
        var ownerName = waystone.getOwnerName();

        var message = new LiteralText("");
        message.append("§6[§eNAME§6]§e=§3" + waystone.getWaystoneName());
        message.append("\n§6[§eGLOBAL§6]§e=§3" + waystone.isGlobal());
        message.append("\n§6[§eHASH§6]§e=§3" + waystone.getHexHash());
        if (owner != null && ownerName != null) {
            message.append("\n§6[§eOWNER-UUID§6]§e=§3" + waystone.getOwner());
            message.append("\n§6[§eOWNER-NAME§6]§e=§3" + waystone.getOwnerName());
        } else {
            message.append("\n§6[§eNO-OWNER§6]");
        }
        player.sendMessage(message, false);

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player)) {
            return ActionResult.PASS;
        }
        var playerAccess = (PlayerEntityMixinAccess) player;

        var message = new LiteralText("");
        message.append("§6[§eNAME§6]§e=§3" + player.getName().getString());
        message.append("\n§6[§eKNOWN-WAYSTONES§6]§e=§3" + playerAccess.getDiscoveredCount());
        message.append("\n§6[§eCOOLDOWN§6]§e=§3" + playerAccess.getTeleportCooldown());
        user.sendMessage(message, false);

        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(new TranslatableText("waystones.debug.debugger_tooltip" + TOOLTIP_MESSAGE));
    }

}
