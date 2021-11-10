package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import wraith.waystones.client.ClientStuff;
import wraith.waystones.item.AbyssWatcherItem;
import wraith.waystones.util.Utils;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

public class AbyssScreenHandler extends UniversalWaystoneScreenHandler {

    public AbyssScreenHandler(int syncId, PlayerInventory inventory) {
        super(CustomScreenHandlerRegistry.ABYSS_SCREEN_HANDLER, syncId, inventory.player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!player.world.isClient) {
            return false;
        }

        int waystoneID = Math.floorDiv(id, 2);
        if (waystoneID >= this.filteredWaystones.size()) {
            return false;
        }

        String waystone = this.filteredWaystones.get(waystoneID);
        if (waystone == null) {
            return false;
        }

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        NbtCompound tag = new NbtCompound();
        tag.putString("waystone_hash", waystone);
        tag.putBoolean("from_abyss_watcher", true);
        data.writeNbt(tag);

        if (id % 2 != 0) {
            this.sortedWaystones.remove(waystone);
            this.filteredWaystones.remove(waystone);
            ClientPlayNetworking.send(Utils.ID("forget_waystone"), data);
            onForget(waystone);
        }
        else {
            if (Utils.canTeleport(player, waystone)) {
                ClientPlayNetworking.send(Utils.ID("teleport_to_waystone"), data);
                playSounds();
            }
            closeScreen();
        }
        return true;
    }

    @Override
    protected void playSounds() {
        if (!player.world.isClient) {
            return;
        }
        super.playSounds();
        ClientStuff.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F);
    }

    @Override
    public void onForget(String waystone) {}

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof AbyssWatcherItem;
    }

}
