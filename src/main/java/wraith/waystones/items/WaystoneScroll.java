package wraith.waystones.items;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.ItemRegistry;

public class WaystoneScroll extends Item {

    public WaystoneScroll(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack scroll = user.getMainHandStack();
        if (!world.isClient) {
            CompoundTag tag = scroll.getTag();
            if (tag == null || !tag.contains("waystones")) {
                return TypedActionResult.fail(scroll);
            }
            tag = tag.getCompound("waystones");
            for (String dimension : tag.getKeys()) {
                ListTag positions = tag.getList(dimension, 11);
                for (int i = 0; i < positions.size(); ++i) {
                    int[] coordinates = positions.getIntArray(i);
                    if (coordinates.length != 3) {
                        continue;
                    }
                    BlockEntity entity = world.getBlockEntity(new BlockPos(coordinates[0], coordinates[1], coordinates[2]));
                    if (entity instanceof WaystoneBlockEntity) {
                        ((PlayerEntityMixinAccess) user).discoverWaystone((WaystoneBlockEntity) entity);
                    }
                }
            }
            user.setStackInHand(hand, new ItemStack(ItemRegistry.ITEMS.get("empty_scroll")));
        }
        return TypedActionResult.success(scroll);
    }
}
