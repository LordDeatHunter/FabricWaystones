package wraith.waystones.block;


import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.Waystones;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.screens.WaystoneScreenHandler;

public class WaystoneBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable {

    public WaystoneBlockEntity() {
        super(BlockEntityRegistry.WAYSTONE_BLOCK_ENTITY);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WaystoneScreenHandler(syncId, pos, this.world.toString());
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText( "container." + Waystones.MOD_ID + ".waystone");
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        return tag;
    }

    @Override
    public void markDirty(){
        if (this.world != null) {
            BlockState blockState = this.getCachedState();
            BlockPos pos2;
            if (blockState.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER) pos2 = this.pos.down();
            else pos2 = this.pos;
            world.markDirty(pos2, world.getBlockEntity(pos2));
            world.markDirty(pos2.up(), world.getBlockEntity(pos2.up()));
            if (!world.getBlockState(pos2).isAir()) {
                this.world.updateComparators(this.pos, world.getBlockState(pos2).getBlock());
            }
            if (!world.getBlockState(pos2.up()).isAir()) {
                this.world.updateComparators(this.pos, world.getBlockState(pos2.up()).getBlock());
            }
        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        CompoundTag tag = new CompoundTag();
        String worldName = this.world.getRegistryKey().getValue().getNamespace() + ":" + this.world.getRegistryKey().getValue().getPath();
        tag.putString("WorldName", worldName);
        tag.putIntArray("Coordinates", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        packetByteBuf.writeCompoundTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(world.getBlockState(pos), tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return toTag(tag);
    }
}
