package wraith.fwaystones.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import wraith.fwaystones.item.map.MapStateAccessor;

@Mixin(FilledMapItem.class)
public class FilledMapItemMixin {

    @ModifyArgs(method = "updateColors", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/map/MapState;removeBanner(Lnet/minecraft/world/BlockView;II)V"))
    private void updateWaystoneMarkers(Args args, World world, Entity entity, MapState state) {
        BlockPos removalPos;
        if ((removalPos = ((MapStateAccessor)state).removeWaystone(world, args.get(1), args.get(2))) != null)
            ((MapStateAccessor)state).addWaystone((WorldAccess) world, removalPos);
    }
}
