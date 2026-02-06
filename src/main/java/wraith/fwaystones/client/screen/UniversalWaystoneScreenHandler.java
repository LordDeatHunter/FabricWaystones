package wraith.fwaystones.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.teleport.TeleportAction;
import wraith.fwaystones.api.teleport.TeleportSource;
import wraith.fwaystones.client.registry.WaystoneScreenHandlers;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.c2s.TeleportToWaystone;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.util.SearchType;
import wraith.fwaystones.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class UniversalWaystoneScreenHandler<D> extends ScreenHandler {

    protected final PlayerEntity player;
    protected List<UUID> sortedWaystones = new ArrayList<>();
    protected List<UUID> filteredWaystones = new ArrayList<>();
    protected String filter = "";
    protected ScreenHandlerType<? extends UniversalWaystoneScreenHandler<?>> type;

    protected D data;

    protected UniversalWaystoneScreenHandler(ScreenHandlerType<? extends UniversalWaystoneScreenHandler<?>> type, int syncId, PlayerEntity player, D data) {
        super(type, syncId);

        this.data = data;
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

        if (setupHandler()) {
            updateWaystones(player);
        } else {
            closeScreen();
        }
    }

    public boolean setupHandler() {
        return true;
    }

    public void updateWaystones(PlayerEntity player) {
        if (!player.getWorld().isClient) return;

        var waystones = new ArrayList<UUID>();

        var data = WaystonePlayerData.getData(player);
        var storage = WaystoneDataStorage.getStorage(player);

        if (data.viewDiscoveredWaystones()) {
            waystones.addAll(data.discoveredWaystones());
        }

        if (data.viewGlobalWaystones()) {
            for (var waystone : storage.getGlobals()) {
                if (!waystones.contains(waystone)) {
                    waystones.add(waystone);
                }
            }
        } else {
            waystones.removeIf(storage::isGlobal);
        }

        this.sortedWaystones = sortUUIDs(waystones);
        filterWaystones();
    }

    public List<UUID> sortUUIDs(List<UUID> waystoneUUIDs) {
        var storage = WaystoneDataStorage.getStorage(player);

        return waystoneUUIDs
            .stream()
            .map(uuid -> {
                return storage.hasPosition(uuid) && storage.getData(uuid) instanceof NetworkedWaystoneData networkedData ? networkedData : null;
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(NetworkedWaystoneData::sortingName, String::compareTo))
            .map(WaystoneData::uuid)
            .collect(Collectors.toList());
    }


    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!player.getWorld().isClient) return false;

        int waystoneID = Math.floorDiv(id, 2);
        if (waystoneID >= this.filteredWaystones.size()) return false;

        var uuid = this.filteredWaystones.get(waystoneID);
        if (uuid == null) return false;

        if (id % 2 != 0) {
            this.sortedWaystones.remove(uuid);
            this.filteredWaystones.remove(uuid);
            onForget(uuid);
            WaystonePlayerData.getData(player).forgetWaystone(uuid);
            updateWaystones(player);

            return true;
        }

        return attemptTeleport(uuid);
    }

    public boolean attemptTeleport(UUID uuid) {
        var source = getTeleportSource(player);

        if (Utils.canTeleportCostLess(player, TeleportAction.of(uuid, source))) {
            WaystoneNetworkHandler.CHANNEL.clientHandle().send(new TeleportToWaystone(uuid, source));

            closeScreen();

            return true;
        }

        return false;
    }

    private static @NotNull TeleportSource getTeleportSource(PlayerEntity player) {
        TeleportSource source;
        var type = player.currentScreenHandler.getType();

        if (type.equals(WaystoneScreenHandlers.WAYSTONE_SCREEN) || type.equals(WaystoneScreenHandlers.EXPERIMENTAL_WAYSTONE_SCREEN)) {
            source = TeleportSource.WAYSTONE;
        } else if (player.currentScreenHandler instanceof PortableWaystoneScreenHandler portableHandler) {
            source = portableHandler.isAbyssal() ? TeleportSource.ABYSS_WATCHER : TeleportSource.POCKET_WORMHOLE;
        } else {
            source = TeleportSource.LOCAL_VOID;
        }
        return source;
    }

    protected void closeScreen() {
        if (player == null) return;

        if (player.getWorld().isClient) {
            closeOnClient();
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.closeHandledScreen();
        }
    }

    @Environment(EnvType.CLIENT)
    protected void closeOnClient() {
        if (!(player instanceof ClientPlayerEntity clientPlayer)) return;

        clientPlayer.closeHandledScreen();

        setCursorStack(ItemStack.EMPTY);
    }

    public void onForget(UUID uuid) {}

    @Override
    public boolean canUse(PlayerEntity player) {
        var ref = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(player, stack -> stack.contains(WaystoneDataComponents.TELEPORTER));

        return ref != null;
    }

    public int getWaystonesCount() {
        return this.filteredWaystones.size();
    }

    public List<UUID> getSearchedWaystones() {
        return this.filteredWaystones;
    }

    public void setFilter(String filter) {
        this.filter = filter.toLowerCase(Locale.ROOT);
    }

    public void filterWaystones() {
        this.filteredWaystones.clear();
        var searchType = WaystonePlayerData.getData(player).waystoneSearchType();
        var storage = WaystoneDataStorage.getStorage(player);
        for (var uuid : this.sortedWaystones) {
            if (!(storage.getData(uuid) instanceof NetworkedWaystoneData networkedData)) continue;
            String name = networkedData.sortingName();
            if ("".equals(this.filter) || searchType.match(name, filter)) {
                filteredWaystones.add(uuid);
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public void toggleSearchType() {
        var data = WaystonePlayerData.getData(player);
        var searchType = data.waystoneSearchType();
        var searchValues = SearchType.values();
        data.waystoneSearchType(searchValues[(searchType.ordinal() + 1) % searchValues.length]);
        filterWaystones();
    }

    public Text getSearchTypeTooltip() {
        return Text.translatable("fwaystones.gui." + (WaystonePlayerData.getData(player).waystoneSearchType().name().toLowerCase()));
    }

}
