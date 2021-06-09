package wraith.waystones.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;

public class LocalVoid extends Item {

    public LocalVoid(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getMainHandStack();
        if (!world.isClient) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("waystone")) {
                return TypedActionResult.fail(stack);
            }
            String hash = tag.getString("waystone");
            WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
            if (waystone == null) {
                stack.setTag(null);
            } else {
                waystone.teleportPlayer(user, false);
                user.getStackInHand(hand).decrement(1);
            }
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockEntity entity = world.getBlockEntity(context.getBlockPos());
        if (!world.isClient && entity instanceof WaystoneBlockEntity) {
            ItemStack stack = context.getStack();
            CompoundTag tag = new CompoundTag();
            tag.putString("waystone", ((WaystoneBlockEntity) entity).getHash());
            stack.setTag(tag);
        }
        return super.useOnBlock(context);
    }

}
