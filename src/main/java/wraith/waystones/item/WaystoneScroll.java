package wraith.waystones.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;

import java.util.HashSet;

public class WaystoneScroll extends Item {

    public WaystoneScroll(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getMainHandStack();
        if (!world.isClient) {
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("waystones")) {
                return TypedActionResult.fail(stack);
            }
            ListTag list = tag.getList("waystones", 8);
            for (int i = 0; i < list.size(); ++i) {
                String hash = list.getString(i);
                WaystoneBlockEntity waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone != null) {
                    ((PlayerEntityMixinAccess) user).discoverWaystone(waystone);
                }
            }
            user.getStackInHand(hand).decrement(1);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof WaystoneBlock) {
            HashSet<String> discovered = ((PlayerEntityMixinAccess) context.getPlayer()).getDiscoveredWaystones();

            ItemStack stack = context.getStack();

            if (discovered.isEmpty()) {
                return ActionResult.FAIL;
            }
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (String hash : discovered) {
                list.add(StringTag.of(hash));
            }
            tag.put("waystones", list);
            stack.setTag(tag);
        }
        return super.useOnBlock(context);
    }

}
