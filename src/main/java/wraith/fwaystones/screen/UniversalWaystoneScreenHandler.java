package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerAccess;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.mixin.ClientPlayerEntityAccessor;
import wraith.fwaystones.mixin.ServerPlayerEntityAccessor;
import wraith.fwaystones.packets.ForgetWaystonePacket;
import wraith.fwaystones.packets.TeleportToWaystonePacket;
import wraith.fwaystones.registry.CustomScreenHandlerRegistry;
import wraith.fwaystones.util.SearchType;
import wraith.fwaystones.util.TeleportSources;
import wraith.fwaystones.util.Utils;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class UniversalWaystoneScreenHandler extends ScreenHandler {

    protected final PlayerEntity player;
    protected ArrayList<String> sortedWaystones = new ArrayList<>();
    protected ArrayList<String> filteredWaystones = new ArrayList<>();
    protected String filter = "";
    protected ScreenHandlerType<? extends UniversalWaystoneScreenHandler> type;

    protected UniversalWaystoneScreenHandler(
        ScreenHandlerType<? extends UniversalWaystoneScreenHandler> type, int syncId,
        PlayerEntity player) {
        super(type, syncId);
        this.player = player;
        this.type = type;
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(
                    new Slot(this.player.getInventory(), x + y * 9 + 9, 2000000000, 2000000000));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(this.player.getInventory(), x, 2000000000, 2000000000));
        }
    }

    public void updateWaystones(PlayerEntity player) {
        if (!player.getWorld().isClient) {
            return;
        }
        this.sortedWaystones = new ArrayList<>();
        if (((PlayerEntityMixinAccess) player).fabricWaystones$shouldViewDiscoveredWaystones()) {
            this.sortedWaystones.addAll(((PlayerAccess) player).fabricWaystones$getHashesSorted());
        }
        if (((PlayerEntityMixinAccess) player).fabricWaystones$shouldViewGlobalWaystones()) {
            for (String waystone : FabricWaystones.WAYSTONE_STORAGE.getGlobals()) {
                if (!this.sortedWaystones.contains(waystone)) {
                    this.sortedWaystones.add(waystone);
                }
            }
        } else {
            this.sortedWaystones.removeIf(FabricWaystones.WAYSTONE_STORAGE::isGlobal);
        }
        this.sortedWaystones.sort(Comparator.comparing(a -> FabricWaystones.WAYSTONE_STORAGE.getName(a)));
        filterWaystones();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!player.getWorld().isClient) {
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

        if (id % 2 != 0) {
            this.sortedWaystones.remove(waystone);
            this.filteredWaystones.remove(waystone);
            onForget(waystone);
            ((PlayerEntityMixinAccess) player).fabricWaystones$forgetWaystone(waystone);
            updateWaystones(player);
            ClientPlayNetworking.send(new ForgetWaystonePacket(waystone));
        } else {
            TeleportSources source = getTeleportSource(player);
            if (Utils.canTeleport(player, waystone, source, false)) {
                ClientPlayNetworking.send(new TeleportToWaystonePacket(waystone, source.name()));
            }
            closeScreen();
        }
        return true;
    }

    private static @NotNull TeleportSources getTeleportSource(PlayerEntity player) {
        TeleportSources source;
        if (player.currentScreenHandler.getType().equals(CustomScreenHandlerRegistry.WAYSTONE_SCREEN)) {
            source = TeleportSources.WAYSTONE;
        } else if (player.currentScreenHandler.getType().equals(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN)) {
            source = TeleportSources.POCKET_WORMHOLE;
        } else if (player.currentScreenHandler.getType().equals(CustomScreenHandlerRegistry.ABYSS_WATCHER_SCREEN)) {
            source = TeleportSources.ABYSS_WATCHER;
        } else {
            source = TeleportSources.LOCAL_VOID;
        }
        return source;
    }

    protected void closeScreen() {
        if (player == null) {
            return;
        }
        if (player.getWorld().isClient) {
            closeOnClient();
        } else {
            ((ServerPlayerEntityAccessor) player).getNetworkHandler()
                .sendPacket(new CloseScreenS2CPacket(this.syncId));
            player.currentScreenHandler.onClosed(player);
            player.currentScreenHandler = player.playerScreenHandler;
        }
    }

    protected void closeOnClient() {
        ((ClientPlayerEntityAccessor) player).getNetworkHandler()
            .sendPacket(new CloseHandledScreenC2SPacket(this.syncId));
        setCursorStack(ItemStack.EMPTY);
        player.currentScreenHandler = player.playerScreenHandler;
        MinecraftClient.getInstance().setScreen(null);
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
        var searchType = ((PlayerEntityMixinAccess) player).fabricWaystones$getSearchType();
        for (String waystone : this.sortedWaystones) {
            String name = FabricWaystones.WAYSTONE_STORAGE.getName(waystone).toLowerCase();
            if ("".equals(this.filter) || searchType.match(name, filter)) {
                filteredWaystones.add(waystone);
            }
        }
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public void toggleSearchType() {
        var playerAccess = (PlayerEntityMixinAccess) player;
        var searchType = playerAccess.fabricWaystones$getSearchType();
        var searchValues = SearchType.values();
        playerAccess.fabricWaystones$setSearchType(searchValues[(searchType.ordinal() + 1) % searchValues.length]);
        filterWaystones();
    }

    public Text getSearchTypeTooltip() {
        return Text.translatable("fwaystones.gui." + (((PlayerEntityMixinAccess) player).fabricWaystones$getSearchType().name().toLowerCase()));
    }

}
