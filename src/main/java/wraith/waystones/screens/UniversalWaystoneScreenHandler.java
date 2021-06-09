package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import wraith.waystones.*;

import java.util.ArrayList;
import java.util.Comparator;

public class UniversalWaystoneScreenHandler extends ScreenHandler {

    private ArrayList<String> sortedWaystones = new ArrayList<>();
    private ArrayList<String> filteredWaystones = new ArrayList<>();
    private String filter = "";

    protected UniversalWaystoneScreenHandler(ScreenHandlerType<? extends UniversalWaystoneScreenHandler> type, int syncId, PlayerEntity player) {
        super(type, syncId);
        updateWaystones(player);
    }

    public void updateWaystones(PlayerEntity player) {
        if (!player.world.isClient) {
            return;
        }
        this.sortedWaystones = new ArrayList<>();
        if (((PlayerEntityMixinAccess)player).shouldViewDiscoveredWaystones()) {
            this.sortedWaystones.addAll(((PlayerAccess)player).getHashesSorted());
        }
        if (((PlayerEntityMixinAccess)player).shouldViewGlobalWaystones()) {
            this.sortedWaystones.addAll(WaystonesClient.WAYSTONE_STORAGE.getGlobals());
        }
        this.sortedWaystones.sort(Comparator.comparing(a -> WaystonesClient.WAYSTONE_STORAGE.getName(a)));
        this.filteredWaystones = new ArrayList<>(this.sortedWaystones);
        filterWaystones();
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
        CompoundTag tag = new CompoundTag();
        tag.putString("waystone_hash", waystone);
        data.writeCompoundTag(tag);

        if (id % 2 != 0) {
            this.sortedWaystones.remove(waystone);
            this.filteredWaystones.remove(waystone);
            ClientPlayNetworking.send(Utils.ID("forget_waystone"), data);
        }
        else {
            ClientPlayNetworking.send(Utils.ID("teleport_to_waystone"), data);
            player.currentScreenHandler = player.playerScreenHandler;
        }
        return true;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

    public int getWaystonesCount() {
        return this.filteredWaystones.size();
    }

    public ArrayList<String> getSearchedWaystones() {
        return this.filteredWaystones;
    }

    public void setFilter(String filter) {
        this.filter = filter.toLowerCase();
    }

    public void filterWaystones() {
        this.filteredWaystones.clear();
        for (String waystone : this.sortedWaystones) {
            if ("".equals(this.filter) || WaystonesClient.WAYSTONE_STORAGE.getName(waystone).toLowerCase().startsWith(this.filter)) {
                filteredWaystones.add(waystone);
            }
        }
    }

}
