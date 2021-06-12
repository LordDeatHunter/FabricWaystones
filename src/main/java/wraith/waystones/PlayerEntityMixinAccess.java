package wraith.waystones;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import wraith.waystones.block.WaystoneBlockEntity;

import java.util.ArrayList;
import java.util.HashSet;

public interface PlayerEntityMixinAccess extends PlayerAccess {

    void discoverWaystone(WaystoneBlockEntity waystone);
    boolean hasDiscoveredWaystone(WaystoneBlockEntity waystone);
    void forgetWaystone(WaystoneBlockEntity waystone);
    void forgetWaystone(String hash);
    void syncData();
    HashSet<String> getDiscoveredWaystones();
    ArrayList<String> getWaystonesSorted();
    void learnWaystones(PlayerEntity player, boolean overwrite);
    void fromTagW(CompoundTag tag);
    CompoundTag toTagW(CompoundTag tag);
    boolean shouldViewGlobalWaystones();
    boolean shouldViewDiscoveredWaystones();
    void toggleViewGlobalWaystones();
    void toggleViewDiscoveredWaystones();
    boolean hasDiscoveredWaystone(String hash);
    void discoverWaystones(HashSet<String> toLearn);
    void forgetWaystones(HashSet<String> toForget);

}
