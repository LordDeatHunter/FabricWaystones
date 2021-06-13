package wraith.waystones.screens;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import wraith.waystones.*;
import wraith.waystones.mixin.ClientPlayerEntityAccessor;
import wraith.waystones.mixin.ServerPlayerEntityAccessor;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class UniversalWaystoneScreenHandler extends ScreenHandler {

    protected final PlayerEntity player;
    protected ArrayList<String> sortedWaystones = new ArrayList<>();
    protected ArrayList<String> filteredWaystones = new ArrayList<>();
    protected String filter = "";

    protected UniversalWaystoneScreenHandler(ScreenHandlerType<? extends UniversalWaystoneScreenHandler> type, int syncId, PlayerEntity player) {
        super(type, syncId);
        updateWaystones(player);
        this.player = player;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(this.player.getInventory(), x + y * 9 + 9, 2000000000, 2000000000));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(this.player.getInventory(), x, 2000000000, 2000000000));
        }
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
            for (String waystone : WaystonesClient.WAYSTONE_STORAGE.getGlobals()) {
                if (!this.sortedWaystones.contains(waystone)) {
                    this.sortedWaystones.add(waystone);
                }
            }
        }
        this.sortedWaystones.sort(Comparator.comparing(a -> WaystonesClient.WAYSTONE_STORAGE.getName(a)));
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
        NbtCompound tag = new NbtCompound();
        tag.putString("waystone_hash", waystone);
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

    protected void closeScreen() {
        if (player == null) {
            return;
        }
        if (player.world.isClient) {
            closeOnClient();
        } else {
            ((ServerPlayerEntityAccessor)player).getNetworkHandler().sendPacket(new CloseScreenS2CPacket(this.syncId));
            player.currentScreenHandler.close(player);
            player.currentScreenHandler = player.playerScreenHandler;
        }
    }

    protected void closeOnClient() {
        ((ClientPlayerEntityAccessor)player).getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(this.syncId));
        setCursorStack(ItemStack.EMPTY);
        player.currentScreenHandler = player.playerScreenHandler;
        MinecraftClient.getInstance().openScreen(null);
    }

    protected void playSounds() {
        if (!player.world.isClient) {
            return;
        }
        ClientStuff.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F);
    }

    public abstract void onForget(String waystone);

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
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
