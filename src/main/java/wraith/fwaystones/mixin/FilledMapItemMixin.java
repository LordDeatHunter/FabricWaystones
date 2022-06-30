package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wraith.fwaystones.item.map.MapStateAccessor;

@Mixin(FilledMapItem.class)
public class FilledMapItemMixin {
    @ModifyReceiver(method = "updateColors", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;removeBanner(Lnet/minecraft/world/BlockView;II)V"))
    private MapState removeWaystones(MapState receiver, BlockView world, int x, int y) {
        BlockPos removalPos;
        if ((removalPos = ((MapStateAccessor)receiver).removeWaystone(world, x, y)) != null)
            ((MapStateAccessor)receiver).addWaystone((WorldAccess) world, removalPos);
        return receiver;
    }
}
