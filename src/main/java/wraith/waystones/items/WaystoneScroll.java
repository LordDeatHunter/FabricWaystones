package wraith.waystones.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import wraith.waystones.Waystones;
import wraith.waystones.registries.ItemRegistry;

public class WaystoneScroll extends Item {

    public WaystoneScroll(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack scroll = user.getMainHandStack();
        CompoundTag tag = scroll.getTag();
        if (tag == null) {
            return TypedActionResult.fail(scroll);
        }
        for(String key : tag.getKeys()) {
            Waystones.WAYSTONE_DATABASE.discoverWaystone(user, key);
        }
        user.setStackInHand(hand, new ItemStack(ItemRegistry.ITEMS.get("empty_scroll")));
        return TypedActionResult.success(scroll);
    }
}
