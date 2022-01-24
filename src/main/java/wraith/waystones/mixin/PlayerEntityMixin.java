package wraith.waystones.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.waystones.Waystones;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityMixinAccess {

    private final HashSet<String> discoveredWaystones = new HashSet<>();
    private boolean viewDiscoveredWaystones = true;
    private boolean viewGlobalWaystones = true;
    private int teleportCooldown = 0;

    private PlayerEntity _this() {
        return (PlayerEntity) (Object) this;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (teleportCooldown <= 0) {
            return;
        }
        teleportCooldown = Math.max(0, teleportCooldown - 1);
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"))
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source == DamageSource.OUT_OF_WORLD) {
            return;
        }
        setTeleportCooldown(Config.getInstance().getCooldownWhenHurt());
    }

    @Override
    public void setTeleportCooldown(int cooldown) {
        if (cooldown > 0) {
            this.teleportCooldown = cooldown;
        }
    }

    @Override
    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    @Override
    public void discoverWaystone(WaystoneBlockEntity waystone) {
        discoveredWaystones.add(waystone.getHash());
        syncData();
    }

    @Override
    public boolean hasDiscoveredWaystone(WaystoneBlockEntity waystone) {
        return discoveredWaystones.contains(waystone.getHash());
    }

    @Override
    public void forgetWaystone(WaystoneBlockEntity waystone) {
        discoveredWaystones.remove(waystone.getHash());
        syncData();
    }

    @Override
    public void forgetWaystone(String hash) {
        discoveredWaystones.remove(hash);
        syncData();
    }

    @Override
    public void syncData() {
        if (!(_this() instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeNbt(toTagW(new NbtCompound()));
        ServerPlayNetworking.send(serverPlayerEntity, Utils.ID("sync_player"), packet);
    }

    @Override
    public HashSet<String> getDiscoveredWaystones() {
        return discoveredWaystones;
    }

    @Override
    public int getDiscoveredCount() {
        return discoveredWaystones.size();
    }

    @Override
    public ArrayList<String> getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(Waystones.WAYSTONE_STORAGE.getWaystone(hash).getWaystoneName());
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(String::compareTo);
        return waystones;
    }

    @Override
    public ArrayList<String> getHashesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(hash);
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(Comparator.comparing(a -> Waystones.WAYSTONE_STORAGE.getWaystone(a).getWaystoneName()));
        return waystones;
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToNbt(NbtCompound tag, CallbackInfo ci) {
        toTagW(tag);
    }

    @Override
    public NbtCompound toTagW(NbtCompound tag) {
        NbtCompound customTag = new NbtCompound();
        NbtList waystones = new NbtList();
        for (String waystone : discoveredWaystones) {
            waystones.add(NbtString.of(waystone));
        }
        customTag.put("discovered_waystones", waystones);
        customTag.putBoolean("view_discovered_waystones", this.viewDiscoveredWaystones);
        customTag.putBoolean("view_global_waystones", this.viewGlobalWaystones);

        tag.put("waystones", customTag);
        return tag;
    }

    @Override
    public void learnWaystones(PlayerEntity player, boolean overwrite) {
        discoveredWaystones.clear();
        this.discoveredWaystones.addAll(((PlayerEntityMixinAccess) player).getDiscoveredWaystones());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        fromTagW(tag);
    }

    @Override
    public void fromTagW(NbtCompound tag) {
        if (!tag.contains("waystones")) {
            return;
        }
        tag = tag.getCompound("waystones");
        if (tag.contains("discovered_waystones")) {
            discoveredWaystones.clear();
            HashSet<String> hashes = new HashSet<>();
            if (Waystones.WAYSTONE_STORAGE != null) {
                hashes = Waystones.WAYSTONE_STORAGE.getAllHashes();
            }
            NbtList waystones = tag.getList("discovered_waystones", 8);
            for (NbtElement waystone : waystones) {
                if (hashes.contains(waystone.asString())) {
                    discoveredWaystones.add(waystone.asString());
                }
            }
        }
        if (tag.contains("view_global_waystones")) {
            this.viewGlobalWaystones = tag.getBoolean("view_global_waystones");
        }
        if (tag.contains("view_discovered_waystones")) {
            this.viewDiscoveredWaystones = tag.getBoolean("view_discovered_waystones");
        }
    }

    @Override
    public boolean shouldViewGlobalWaystones() {
        return this.viewGlobalWaystones;
    }

    @Override
    public boolean shouldViewDiscoveredWaystones() {
        return this.viewDiscoveredWaystones;
    }

    @Override
    public void toggleViewGlobalWaystones() {
        this.viewGlobalWaystones = !this.viewGlobalWaystones;
        syncData();
    }

    @Override
    public void toggleViewDiscoveredWaystones() {
        this.viewDiscoveredWaystones = !this.viewDiscoveredWaystones;
        syncData();
    }

    @Override
    public boolean hasDiscoveredWaystone(String hash) {
        return this.discoveredWaystones.contains(hash);
    }

    @Override
    public void discoverWaystones(HashSet<String> toLearn) {
        if (Waystones.WAYSTONE_STORAGE == null) {
            return;
        }
        for (String waystone : toLearn) {
            if (!Waystones.WAYSTONE_STORAGE.containsHash(waystone)) {
                continue;
            }
            this.discoveredWaystones.add(waystone);
        }
        syncData();
    }

    @Override
    public void forgetWaystones(HashSet<String> toForget) {
        for (String hash : toForget) {
            discoveredWaystones.remove(hash);
        }
        syncData();
    }

}
