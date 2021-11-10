package wraith.waystones.item;

import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;

public class WaystoneDebugger extends Item {

    public WaystoneDebugger(Settings settings) {
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
        message.append("\n§6[§eHASH§6]§e=§3" + waystone.getHexHash());
        if (owner != null && ownerName != null) {
            message.append("\n§6[§eOWNER-UUID§6]§e=§3" + waystone.getOwner());
            message.append("\n§6[§eOWNER-NAME§6]§e=§3" + waystone.getOwnerName());
        } else {
            message.append("\n§6[§eNO-OWNER§6]§e=§3");
        }
        player.sendMessage(message, false);

        return super.useOnBlock(context);
    }

}
