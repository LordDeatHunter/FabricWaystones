package wraith.fwaystones.client.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.c2s.ToggleGlobalWaystone;
import wraith.fwaystones.client.registry.WaystoneScreenHandlers;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.api.core.WaystoneAccess;

import java.util.UUID;
import java.util.function.Function;

public class WaystoneBlockScreenHandler extends UniversalWaystoneScreenHandler<WaystoneAccess> {

    private boolean isClient;

    private UUID uuid;
    private WaystonePosition hash;

    private String name;

    private UUID owner;
    private String ownerName = "";

    private boolean isGlobal;
    private Function<PlayerEntity, Boolean> canUse = null;

    public WaystoneBlockScreenHandler(int syncId, PlayerInventory playerInventory, WaystoneAccess hashable) {
        super(WaystoneScreenHandlers.WAYSTONE_SCREEN, syncId, playerInventory.player, hashable);
    }

    @Override
    public void setupHandler() {
        this.isClient = player.getWorld().isClient;

        this.hash = data.position();
        var waystoneData = WaystoneDataStorage.getStorage(player).getData(hash);
        this.uuid = waystoneData.uuid();

        this.name = waystoneData.name();

        this.owner = waystoneData.owner();
        this.ownerName = waystoneData.ownerName();

        this.isGlobal = waystoneData.global();
        this.canUse = data::canAccess;
    }

    @Override
    public void onForget(UUID uuid) {
        if (this.uuid.equals(uuid)) {
            closeScreen();
        }
    }

    @Override
    public void updateWaystones(PlayerEntity player) {
        super.updateWaystones(player);

        if (!player.getWorld().isClient) return;

        if (!WaystoneDataStorage.getStorage(player).hasData(this.hash)) {
            closeScreen();
        }

        if (!this.sortedWaystones.contains(this.uuid)) {
            this.sortedWaystones.add(this.uuid);
            this.filterWaystones();
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse != null ? canUse.apply(player) : true;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public WaystonePosition position() {
        return this.hash;
    }

    public void toggleGlobal() {
        if (!isClient) return;

        WaystoneNetworkHandler.CHANNEL.clientHandle().send(new ToggleGlobalWaystone(this.uuid));

        this.isGlobal = !this.isGlobal;
    }

    public boolean isOwner(PlayerEntity player) {
        return this.owner != null && this.owner.equals(player.getUuid());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGlobal() {
        return this.isGlobal;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName == null ? "" : this.ownerName;
    }

    public void removeOwner() {
        this.owner = null;
        this.ownerName = null;
    }

    public boolean hasOwner() {
        return this.owner != null;
    }

}
