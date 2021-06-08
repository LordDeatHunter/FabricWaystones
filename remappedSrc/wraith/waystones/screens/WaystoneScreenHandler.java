package wraith.waystones.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.CustomScreenHandlerRegistry;

public class WaystoneScreenHandler extends UniversalWaystoneScreenHandler {

    private final String world;
    private final BlockPos pos;

    public WaystoneScreenHandler(int syncId, BlockPos pos, String world, WaystoneBlockEntity waystoneEntity) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        this.pos = pos;
        this.world = world;
    }

    public WaystoneScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        NbtCompound tag = buf.readNbt();
        int[] coords = tag.getIntArray("Coordinates");
        this.pos = new BlockPos(coords[0], coords[1], coords[2]);
        this.world = tag.getString("WorldName");
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public String getWorld() {
        return this.world;
    }

    public String getName() {
        return getWaystone().name;
    }

    private Waystone getWaystone() {
        return Waystones.WAYSTONE_DATABASE.getWaystone(pos, world);
    }

}
