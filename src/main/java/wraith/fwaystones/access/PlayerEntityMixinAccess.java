package wraith.fwaystones.access;

import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.SearchType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface PlayerEntityMixinAccess extends PlayerAccess {

    void fabricWaystones$discoverWaystone(WaystoneBlockEntity waystone);

    void fabricWaystones$discoverWaystone(String hash);

    void fabricWaystones$discoverWaystone(String hash, boolean sync);

    boolean fabricWaystones$hasDiscoveredWaystone(WaystoneBlockEntity waystone);

    void fabricWaystones$forgetWaystone(WaystoneBlockEntity waystone);

    void fabricWaystones$forgetWaystone(String hash);

    void fabricWaystones$forgetWaystone(String hash, boolean sync);

    void fabricWaystones$syncData();

    Set<String> fabricWaystones$getDiscoveredWaystones();

    ArrayList<String> fabricWaystones$getWaystonesSorted();

    void fabricWaystones$learnWaystones(Player player);

    void fabricWaystones$fromTagW(CompoundTag tag);

    CompoundTag fabricWaystones$toTagW(CompoundTag tag);

    boolean fabricWaystones$shouldViewGlobalWaystones();

    boolean fabricWaystones$shouldViewDiscoveredWaystones();

    void fabricWaystones$toggleViewGlobalWaystones();

    void fabricWaystones$toggleViewDiscoveredWaystones();

    boolean fabricWaystones$hasDiscoveredWaystone(String hash);

    void fabricWaystones$discoverWaystones(HashSet<String> toLearn);

    void fabricWaystones$forgetWaystones(HashSet<String> toForget);

    int fabricWaystones$getTeleportCooldown();

    void fabricWaystones$setTeleportCooldown(int cooldown);

    void fabricWaystones$forgetAllWaystones();
    boolean fabricWaystones$autofocusWaystoneFields();
    void fabricWaystones$toggleAutofocusWaystoneFields();
    SearchType fabricWaystones$getSearchType();
    void fabricWaystones$setSearchType(SearchType searchType);
}
