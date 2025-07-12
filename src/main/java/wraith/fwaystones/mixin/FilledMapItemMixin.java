package wraith.fwaystones.mixin;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.map.MapState;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.block.AbstractWaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.pond.MapStateDuck;

import static wraith.fwaystones.registry.WaystoneBlocks.WAYSTONE;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin {

    @Shadow
    public static @Nullable MapState getMapState(ItemStack map, World world) {
        return null;
    }

    @Inject(method = "useOnBlock", at = @At(value = "HEAD"), cancellable = true)
    private void addOrRemoveWaystonesFromMaps(
        ItemUsageContext context,
        CallbackInfoReturnable<ActionResult> cir
    ) {
        var world = context.getWorld();

        var contextState = world.getBlockState(context.getBlockPos());
        if (!(contextState.getBlock() instanceof AbstractWaystoneBlock waystoneBlock)) return;

        var targetPos = waystoneBlock.getBasePos(context.getBlockPos(), contextState);
        var state = world.getBlockState(targetPos);
        if (!state.isOf(WAYSTONE)) return;
        var blockEntity = world.getBlockEntity(targetPos);
        if (!(blockEntity instanceof WaystoneBlockEntity waystoneBlockEntity)) return;

        var mapState = getMapState(context.getStack(), world);
        if (mapState == null) return;

        var storage = WaystoneDataStorage.getStorage(world);
        if (storage == null) return;

        var waystoneData = waystoneBlockEntity.getData();
        if (waystoneData == null) return;
        var waystonePosition = storage.getPosition(waystoneData);

        var success = world.isClient || ((MapStateDuck) mapState).fwaystones$toggleWaystoneMarker(waystoneData, waystonePosition);

        if (success) cir.setReturnValue(ActionResult.success(context.getWorld().isClient));
        mapState.markDirty();
    }
}
