package wraith.fwaystones.mixin;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.SearchType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Player.class)
public class PlayerEntityMixin implements PlayerEntityMixinAccess {
    private final Set<String> discoveredWaystones = ConcurrentHashMap.newKeySet();
    private boolean viewDiscoveredWaystones = true;
    private boolean viewGlobalWaystones = true;
    private boolean autofocusWaystoneFields = true;
    private SearchType waystoneSearchType = SearchType.CONTAINS;
    private int teleportCooldown = 0;
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
    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source.isBypassInvul()) {
            return;
        }
        setTeleportCooldown(Waystones.CONFIG.teleportation_cooldown.cooldown_ticks_when_hurt);
    }
    @Override
    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    @Override
    public void setTeleportCooldown(int cooldown) {
        if (cooldown > 0) {
            this.teleportCooldown = cooldown;
        }
    }

    @Override
    public void discoverWaystone(WaystoneBlockEntity waystone) {
        discoverWaystone(waystone.getHash());
    }

    @Override
    public void discoverWaystone(String hash) {
        discoverWaystone(hash, true);
    }

    @Override
    public void discoverWaystone(String hash, boolean sync) {
        //WaystoneEvents.DISCOVER_WAYSTONE_EVENT.invoker().onUpdate(hash);
        discoveredWaystones.add(hash);
        if (sync) {
            syncData();
        }
    }

    @Override
    public boolean hasDiscoveredWaystone(WaystoneBlockEntity waystone) {
        return discoveredWaystones.contains(waystone.getHash());
    }

    @Override
    public void forgetWaystone(WaystoneBlockEntity waystone) {
        forgetWaystone(waystone.getHash());
    }

    @Override
    public void forgetWaystone(String hash) {
        forgetWaystone(hash, true);
    }

    @Override
    public void forgetWaystone(String hash, boolean sync) {
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
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
        //TODO: WaystoneEvents.REMOVE_WAYSTONE_EVENT.invoker().onRemove(hash);
        discoveredWaystones.remove(hash);
        if (sync) {
            syncData();
        }
    }

    @Override
    public void syncData() {
        if (!(_this() instanceof ServerPlayer serverPlayerEntity)) {
            return;
        }
        FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeNbt(toTagW(new CompoundTag()));
        NetworkManager.sendToPlayer(serverPlayerEntity, PacketHandler.SYNC_PLAYER, packet);
    }

    @Override
    public Set<String> getDiscoveredWaystones() {
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
                waystones.add(Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getWaystoneName());
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

        waystones.sort(Comparator.comparing(
                a -> Waystones.WAYSTONE_STORAGE.getWaystoneEntity(a).getWaystoneName()));
        return waystones;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void writeCustomDataToNbt(CompoundTag tag, CallbackInfo ci) {
        toTagW(tag);
    }

    @Override
    public CompoundTag toTagW(CompoundTag tag) {
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

        tag.put(Waystones.MOD_ID, customTag);
        return tag;
    }

    @Override
    public void learnWaystones(Player player) {
        discoveredWaystones.clear();
        ((PlayerEntityMixinAccess) player).getDiscoveredWaystones().forEach(hash -> discoverWaystone(hash, false));
        syncData();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readCustomDataFromNbt(CompoundTag tag, CallbackInfo ci) {
        fromTagW(tag);
    }

    @Override
    public void fromTagW(CompoundTag tag) {
        if (!tag.contains(Waystones.MOD_ID)) {
            return;
        }
        tag = tag.getCompound(Waystones.MOD_ID);
        if (tag.contains("discovered_waystones")) {
            var oldDiscovered = new HashSet<>(discoveredWaystones);
            discoveredWaystones.clear();
            HashSet<String> hashes = new HashSet<>();
            if (Waystones.WAYSTONE_STORAGE != null) {
                hashes = Waystones.WAYSTONE_STORAGE.getAllHashes();
            }
            tag.getList("discovered_waystones", Tag.TAG_STRING)
                    .stream()
                    .map(Tag::getAsString)
                    .filter(hashes::contains)
                    .forEach(hash -> {
                        discoveredWaystones.add(hash);
                        /*if (!oldDiscovered.contains(hash)) {
                            WaystoneEvents.DISCOVER_WAYSTONE_EVENT.invoker().onUpdate(hash);
                        }*/
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
        if (tag.contains("waystone_search_type")) {
            try {
                this.waystoneSearchType = SearchType.valueOf(tag.getString("waystone_search_type"));
            } catch (IllegalArgumentException e) {
                Waystones.LOGGER.warn("Received invalid waystone search type: " + tag.getString("waystone_search_type"));
            }
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
        toLearn.forEach(hash -> discoverWaystone(hash, false));
        syncData();
    }

    @Override
    public void forgetWaystones(HashSet<String> toForget) {
        toForget.forEach(hash -> this.forgetWaystone(hash, false));
        syncData();
    }

    @Override
    public void forgetAllWaystones() {
        discoveredWaystones.clear();
        //WaystoneEvents.FORGET_ALL_WAYSTONES_EVENT.invoker().onForgetAll(_this());
        ////discoveredWaystones.forEach(hash -> forgetWaystone(hash, false));
        syncData();
    }

    @Override
    public boolean autofocusWaystoneFields() {
        return autofocusWaystoneFields;
    }

    @Override
    public void toggleAutofocusWaystoneFields() {
        autofocusWaystoneFields = !autofocusWaystoneFields;
    }

    @Override
    public SearchType getSearchType() {
        return waystoneSearchType;
    }

    @Override
    public void setSearchType(SearchType searchType) {
        this.waystoneSearchType = searchType;
    }

}
