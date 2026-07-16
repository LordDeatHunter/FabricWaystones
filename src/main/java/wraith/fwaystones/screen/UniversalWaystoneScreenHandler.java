package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

public abstract class UniversalWaystoneScreenHandler extends AbstractContainerMenu {

    protected final Player player;
    protected ArrayList<String> sortedWaystones = new ArrayList<>();
    protected ArrayList<String> filteredWaystones = new ArrayList<>();
    protected String filter = "";
    protected MenuType<? extends UniversalWaystoneScreenHandler> type;

    protected UniversalWaystoneScreenHandler(
        MenuType<? extends UniversalWaystoneScreenHandler> type, int syncId,
        Player player) {
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

    public void updateWaystones(Player player) {
        if (!player.level().isClientSide) {
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
    public boolean clickMenuButton(Player player, int id) {
        if (!player.level().isClientSide) {
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

    private static @NotNull TeleportSources getTeleportSource(Player player) {
        TeleportSources source;
        if (player.containerMenu.getType().equals(CustomScreenHandlerRegistry.WAYSTONE_SCREEN)) {
            source = TeleportSources.WAYSTONE;
        } else if (player.containerMenu.getType().equals(CustomScreenHandlerRegistry.POCKET_WORMHOLE_SCREEN)) {
            source = TeleportSources.POCKET_WORMHOLE;
        } else if (player.containerMenu.getType().equals(CustomScreenHandlerRegistry.ABYSS_WATCHER_SCREEN)) {
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
        if (player.level().isClientSide) {
            closeOnClient();
        } else {
            ((ServerPlayerEntityAccessor) player).getNetworkHandler()
                .send(new ClientboundContainerClosePacket(this.containerId));
            player.containerMenu.removed(player);
            player.containerMenu = player.inventoryMenu;
        }
    }

    protected void closeOnClient() {
        ((ClientPlayerEntityAccessor) player).getNetworkHandler()
            .send(new ServerboundContainerClosePacket(this.containerId));
        setCarried(ItemStack.EMPTY);
        player.containerMenu = player.inventoryMenu;
        Minecraft.getInstance().setScreen(null);
    }

    public abstract void onForget(String waystone);

    @Override
    public boolean stillValid(Player player) {
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
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public void toggleSearchType() {
        var playerAccess = (PlayerEntityMixinAccess) player;
        var searchType = playerAccess.fabricWaystones$getSearchType();
        var searchValues = SearchType.values();
        playerAccess.fabricWaystones$setSearchType(searchValues[(searchType.ordinal() + 1) % searchValues.length]);
        filterWaystones();
    }

    public Component getSearchTypeTooltip() {
        return Component.translatable("fwaystones.gui." + (((PlayerEntityMixinAccess) player).fabricWaystones$getSearchType().name().toLowerCase()));
    }

}
