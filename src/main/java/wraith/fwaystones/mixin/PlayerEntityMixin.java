package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

@Mixin(Player.class)
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
    private Player _this() {
        return (Player) (Object) this;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(CallbackInfo ci) {
        if (teleportCooldown <= 0) {
            return;
        }
        teleportCooldown = Math.max(0, teleportCooldown - 1);
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInvulnerableTo(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    public boolean applyDamage(Player instance, ServerLevel world, DamageSource source, Operation<Boolean> original) {
        if (!original.call(instance, world, source)) {
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
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
            if ((server != null && !server.isDedicatedServer()) || player.getUUID().equals(waystone.getOwner())) {
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
        if (!(_this() instanceof ServerPlayer serverPlayerEntity)) {
            return;
        }
        ServerPlayNetworking.send(serverPlayerEntity, new SyncPlayerPacket(fabricWaystones$toTagW(new CompoundTag())));
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


    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void writeCustomDataToNbt(ValueOutput output, CallbackInfo ci) {
        CompoundTag tag = fabricWaystones$toTagW(new CompoundTag());
        output.store(FabricWaystones.MOD_ID, CompoundTag.CODEC, tag.getCompoundOrEmpty(FabricWaystones.MOD_ID));
    }

    @Override
    public CompoundTag fabricWaystones$toTagW(CompoundTag tag) {
        CompoundTag customTag = new CompoundTag();
        ListTag waystones = new ListTag();
        for (String waystone : discoveredWaystones) {
            waystones.add(StringTag.valueOf(waystone));
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
    public void fabricWaystones$learnWaystones(Player player) {
        discoveredWaystones.clear();
        int oldCount = fabricWaystones$getDiscoveredCount();
        ((PlayerEntityMixinAccess) player).fabricWaystones$getDiscoveredWaystones().forEach(hash -> fabricWaystones$discoverWaystone(hash, false));
        if (oldCount != fabricWaystones$getDiscoveredCount()) {
            fabricWaystones$syncData();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readCustomDataFromNbt(ValueInput input, CallbackInfo ci) {
        input.read(FabricWaystones.MOD_ID, CompoundTag.CODEC).ifPresent(custom -> {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put(FabricWaystones.MOD_ID, custom);
            fabricWaystones$fromTagW(wrapper);
        });
    }

    @Override
    public void fabricWaystones$fromTagW(CompoundTag tag) {
        if (!tag.contains(FabricWaystones.MOD_ID)) {
            return;
        }
        tag = tag.getCompoundOrEmpty(FabricWaystones.MOD_ID);
        if (tag.contains("discovered_waystones")) {
            var oldDiscovered = new HashSet<>(discoveredWaystones);
            discoveredWaystones.clear();
            HashSet<String> hashes = new HashSet<>();
            if (FabricWaystones.WAYSTONE_STORAGE != null) {
                hashes = FabricWaystones.WAYSTONE_STORAGE.getAllHashes();
            }
            tag.getListOrEmpty("discovered_waystones")
                .stream()
                .flatMap(element -> element.asString().stream())
                .filter(hashes::contains)
                .forEach(hash -> {
                    discoveredWaystones.add(hash);
                    if (!oldDiscovered.contains(hash)) {
                        WaystoneEvents.DISCOVER_WAYSTONE_EVENT.invoker().onUpdate(hash);
                    }
                });
        }
        tag.getBoolean("view_global_waystones").ifPresent(value -> this.viewGlobalWaystones = value);
        tag.getBoolean("view_discovered_waystones").ifPresent(value -> this.viewDiscoveredWaystones = value);
        tag.getBoolean("autofocus_waystone_fields").ifPresent(value -> this.autofocusWaystoneFields = value);
        tag.getInt("teleportCooldown").ifPresent(value -> this.teleportCooldown = value);
        if (tag.contains("waystone_search_type")) {
            String searchType = tag.getStringOr("waystone_search_type", "");
            try {
                this.waystoneSearchType = SearchType.valueOf(searchType);
            } catch (IllegalArgumentException e) {
                FabricWaystones.LOGGER.warn("Received invalid waystone search type: " + searchType);
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
