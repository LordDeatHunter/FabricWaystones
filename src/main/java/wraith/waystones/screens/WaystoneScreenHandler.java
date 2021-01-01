package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.Utils;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.CustomScreenHandlerRegistry;

public class WaystoneScreenHandler extends ScreenHandler {

    private final String world;
    private final BlockPos pos;
    private final WaystoneBlockEntity waystoneEntity;

    public WaystoneScreenHandler(int syncId, BlockPos pos, String world, WaystoneBlockEntity waystoneEntity) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        this.pos = pos;
        this.world = world;
        this.waystoneEntity = waystoneEntity;
    }

    public WaystoneScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(CustomScreenHandlerRegistry.WAYSTONE_SCREEN, syncId);
        int n;
        int m;
        for(n = 0; n < 3; ++n) {
            for(m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m + (n + 1) * 9, 8 + m * 18, 104 + n * 18));
            }
        }

        for(n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 162));
        }

        CompoundTag tag = buf.readCompoundTag();
        int[] coords = tag.getIntArray("Coordinates");
        this.pos = new BlockPos(coords[0], coords[1], coords[2]);
        this.world = tag.getString("WorldName");
        this.waystoneEntity = null;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
            Waystone waystone = Waystones.WAYSTONE_DATABASE.getWaystoneFromClick(player, id);
            if (waystone != null && Utils.canTeleport(player)) {
                Waystones.teleportPlayer(player, waystone.world, waystone.facing, waystone.pos);
                Utils.consumePayment(player, this.waystoneEntity);
                return true;
            }
            return false;
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
