package wraith.fwaystones.access;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.SearchType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    void fabricWaystones$learnWaystones(PlayerEntity player);

    void fabricWaystones$fromTagW(NbtCompound tag);

    NbtCompound fabricWaystones$toTagW(NbtCompound tag);

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
