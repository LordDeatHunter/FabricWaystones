package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.integration.event.WaystoneEvents;
import wraith.fwaystones.packets.client.SyncPlayerPacket;
import wraith.fwaystones.util.SearchType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityMixinAccess {

    @Unique
    private final Set<String> discoveredWaystones = ConcurrentHashMap.newKeySet();
    @Unique
    private boolean viewDiscoveredWaystones = true;
    @Unique
    private boolean viewGlobalWaystones = true;
    @Unique
    private boolean autofocusWaystoneFields = true;
    @Unique
    private SearchType waystoneSearchType = SearchType.CONTAINS;
    @Unique
    private int teleportCooldown = 0;

    @Unique
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

    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInvulnerableTo(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)Z"))
    public boolean applyDamage(PlayerEntity instance, ServerWorld world, DamageSource source, Operation<Boolean> original) {
        if (!original.call(instance, world, source)) {
            if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                fabricWaystones$setTeleportCooldown(FabricWaystones.CONFIG.teleportation_cooldown.cooldown_ticks_when_hurt());
            }
        }
        return original.call(instance, world, source);
    }

    @Override
    public int fabricWaystones$getTeleportCooldown() {
        return teleportCooldown;
    }

    @Override
    public void fabricWaystones$setTeleportCooldown(int cooldown) {
        if (cooldown > 0) {
            this.teleportCooldown = cooldown;
        }
    }

    @Override
    public void fabricWaystones$discoverWaystone(WaystoneBlockEntity waystone) {
        fabricWaystones$discoverWaystone(waystone.getHash());
    }

    @Override
    public void fabricWaystones$discoverWaystone(String hash) {
        fabricWaystones$discoverWaystone(hash, true);
    }

    @Override
    public void fabricWaystones$discoverWaystone(String hash, boolean sync) {
        WaystoneEvents.DISCOVER_WAYSTONE_EVENT.invoker().onUpdate(hash);
        discoveredWaystones.add(hash);
        if (sync) {
            fabricWaystones$syncData();
        }
    }

    @Override
    public boolean fabricWaystones$hasDiscoveredWaystone(WaystoneBlockEntity waystone) {
        return discoveredWaystones.contains(waystone.getHash());
    }

    @Override
    public void fabricWaystones$forgetWaystone(WaystoneBlockEntity waystone) {
        fabricWaystones$forgetWaystone(waystone.getHash());
    }

    @Override
    public void fabricWaystones$forgetWaystone(String hash) {
        fabricWaystones$forgetWaystone(hash, true);
    }

    @Override
    public void fabricWaystones$forgetWaystone(String hash, boolean sync) {
        var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
        var player = _this();
        if (waystone != null) {
            if (waystone.isGlobal()) {
                return;
            }
            var server = player.getServer();
            if ((server != null && !server.isDedicated()) || player.getUuid().equals(waystone.getOwner())) {
                waystone.setOwner(null);
            }
        }
        WaystoneEvents.REMOVE_WAYSTONE_EVENT.invoker().onRemove(hash);
        discoveredWaystones.remove(hash);
        if (sync) {
            fabricWaystones$syncData();
        }
    }

    @Override
    public void fabricWaystones$syncData() {
        if (!(_this() instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }
        ServerPlayNetworking.send(serverPlayerEntity, new SyncPlayerPacket(fabricWaystones$toTagW(new NbtCompound())));
    }

    @Override
    public Set<String> fabricWaystones$getDiscoveredWaystones() {
        return discoveredWaystones;
    }

    @Override
    public int fabricWaystones$getDiscoveredCount() {
        return discoveredWaystones.size();
    }

    @Override
    public ArrayList<String> fabricWaystones$getWaystonesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getWaystoneName());
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
    public ArrayList<String> fabricWaystones$getHashesSorted() {
        ArrayList<String> waystones = new ArrayList<>();
        HashSet<String> toRemove = new HashSet<>();
        for (String hash : discoveredWaystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                waystones.add(hash);
            } else {
                toRemove.add(hash);
            }
        }
        for (String remove : toRemove) {
            discoveredWaystones.remove(remove);
        }

        waystones.sort(Comparator.comparing(
            a -> FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(a).getWaystoneName()));
        return waystones;
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeCustomDataToNbt(NbtCompound tag, CallbackInfo ci) {
        fabricWaystones$toTagW(tag);
    }

    @Override
    public NbtCompound fabricWaystones$toTagW(NbtCompound tag) {
        NbtCompound customTag = new NbtCompound();
        NbtList waystones = new NbtList();
        for (String waystone : discoveredWaystones) {
            waystones.add(NbtString.of(waystone));
        }
        customTag.put("discovered_waystones", waystones);
        customTag.putBoolean("view_discovered_waystones", this.viewDiscoveredWaystones);
        customTag.putBoolean("view_global_waystones", this.viewGlobalWaystones);
        customTag.putBoolean("autofocus_waystone_fields", this.autofocusWaystoneFields);
        customTag.putString("waystone_search_type", this.waystoneSearchType.name());
        customTag.putInt("teleportCooldown", this.teleportCooldown);

        tag.put(FabricWaystones.MOD_ID, customTag);
        return tag;
    }

    @Override
    public void fabricWaystones$learnWaystones(PlayerEntity player) {
        discoveredWaystones.clear();
        int oldCount = fabricWaystones$getDiscoveredCount();
        ((PlayerEntityMixinAccess) player).fabricWaystones$getDiscoveredWaystones().forEach(hash -> fabricWaystones$discoverWaystone(hash, false));
        if (oldCount != fabricWaystones$getDiscoveredCount()) {
            fabricWaystones$syncData();
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readCustomDataFromNbt(NbtCompound tag, CallbackInfo ci) {
        fabricWaystones$fromTagW(tag);
    }

    @Override
    public void fabricWaystones$fromTagW(NbtCompound tag) {
        if (!tag.contains(FabricWaystones.MOD_ID)) {
            return;
        }
        tag = tag.getCompound(FabricWaystones.MOD_ID);
        if (tag.contains("discovered_waystones")) {
            var oldDiscovered = new HashSet<>(discoveredWaystones);
            discoveredWaystones.clear();
            HashSet<String> hashes = new HashSet<>();
            if (FabricWaystones.WAYSTONE_STORAGE != null) {
                hashes = FabricWaystones.WAYSTONE_STORAGE.getAllHashes();
            }
            tag.getList("discovered_waystones", NbtElement.STRING_TYPE)
                .stream()
                .map(NbtElement::asString)
                .filter(hashes::contains)
                .forEach(hash -> {
                    discoveredWaystones.add(hash);
                    if (!oldDiscovered.contains(hash)) {
                        WaystoneEvents.DISCOVER_WAYSTONE_EVENT.invoker().onUpdate(hash);
                    }
                });
        }
        if (tag.contains("view_global_waystones")) {
            this.viewGlobalWaystones = tag.getBoolean("view_global_waystones");
        }
        if (tag.contains("view_discovered_waystones")) {
            this.viewDiscoveredWaystones = tag.getBoolean("view_discovered_waystones");
        }
        if (tag.contains("autofocus_waystone_fields")) {
            this.autofocusWaystoneFields = tag.getBoolean("autofocus_waystone_fields");
        }
        if (tag.contains("teleportCooldown")) {
            this.teleportCooldown = tag.getInt("teleportCooldown");
        }
        if (tag.contains("waystone_search_type")) {
            try {
                this.waystoneSearchType = SearchType.valueOf(tag.getString("waystone_search_type"));
            } catch (IllegalArgumentException e) {
                FabricWaystones.LOGGER.warn("Received invalid waystone search type: " + tag.getString("waystone_search_type"));
            }
        }
    }

    @Override
    public boolean fabricWaystones$shouldViewGlobalWaystones() {
        return this.viewGlobalWaystones;
    }

    @Override
    public boolean fabricWaystones$shouldViewDiscoveredWaystones() {
        return this.viewDiscoveredWaystones;
    }

    @Override
    public void fabricWaystones$toggleViewGlobalWaystones() {
        this.viewGlobalWaystones = !this.viewGlobalWaystones;
        fabricWaystones$syncData();
    }

    @Override
    public void fabricWaystones$toggleViewDiscoveredWaystones() {
        this.viewDiscoveredWaystones = !this.viewDiscoveredWaystones;
        fabricWaystones$syncData();
    }

    @Override
    public boolean fabricWaystones$hasDiscoveredWaystone(String hash) {
        return this.discoveredWaystones.contains(hash);
    }

    @Override
    public void fabricWaystones$discoverWaystones(HashSet<String> toLearn) {
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return;
        }
        toLearn.forEach(hash -> fabricWaystones$discoverWaystone(hash, false));
        if (!toLearn.isEmpty()) {
            fabricWaystones$syncData();
        }
    }

    @Override
    public void fabricWaystones$forgetWaystones(HashSet<String> toForget) {
        toForget.forEach(hash -> this.fabricWaystones$forgetWaystone(hash, false));
        if (!toForget.isEmpty()) {
            fabricWaystones$syncData();
        }
    }

    @Override
    public void fabricWaystones$forgetAllWaystones() {
        if (discoveredWaystones.isEmpty()) {
            return;
        }
        fabricWaystones$forgetWaystones(new HashSet<>(discoveredWaystones));
        WaystoneEvents.FORGET_ALL_WAYSTONES_EVENT.invoker().onForgetAll(_this());
    }

    @Override
    public boolean fabricWaystones$autofocusWaystoneFields() {
        return autofocusWaystoneFields;
    }

    @Override
    public void fabricWaystones$toggleAutofocusWaystoneFields() {
        autofocusWaystoneFields = !autofocusWaystoneFields;
    }

    @Override
    public SearchType fabricWaystones$getSearchType() {
        return waystoneSearchType;
    }

    @Override
    public void fabricWaystones$setSearchType(SearchType searchType) {
        this.waystoneSearchType = searchType;
    }

}
