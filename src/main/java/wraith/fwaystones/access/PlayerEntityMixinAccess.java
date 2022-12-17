package wraith.fwaystones.access;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import wraith.fwaystones.block.WaystoneBlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface PlayerEntityMixinAccess extends PlayerAccess {

    void discoverWaystone(WaystoneBlockEntity waystone);

    void discoverWaystone(String hash);

    void discoverWaystone(String hash, boolean sync);

    boolean hasDiscoveredWaystone(WaystoneBlockEntity waystone);

    void forgetWaystone(WaystoneBlockEntity waystone);

    void forgetWaystone(String hash);

    void forgetWaystone(String hash, boolean sync);

    void syncData();

    Set<String> getDiscoveredWaystones();

    ArrayList<String> getWaystonesSorted();

    void learnWaystones(PlayerEntity player);

    void fromTagW(NbtCompound tag);

    NbtCompound toTagW(NbtCompound tag);

    boolean shouldViewGlobalWaystones();

    boolean shouldViewDiscoveredWaystones();

    void toggleViewGlobalWaystones();

    void toggleViewDiscoveredWaystones();

    boolean hasDiscoveredWaystone(String hash);

    void discoverWaystones(HashSet<String> toLearn);

    void forgetWaystones(HashSet<String> toForget);

    int getTeleportCooldown();

    void setTeleportCooldown(int cooldown);

    void forgetAllWaystones();
}
