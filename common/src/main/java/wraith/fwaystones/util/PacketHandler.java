package wraith.fwaystones.util;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.registry.ItemReg;

import java.util.HashSet;
import java.util.UUID;

public class PacketHandler {
    public static final ResourceLocation FORGET_WAYSTONE = Utils.ID("forget_waystone");
    public static final ResourceLocation REMOVE_WAYSTONE_OWNER = Utils.ID("remove_waystone_owner");
    public static final ResourceLocation RENAME_WAYSTONE = Utils.ID("rename_waystone");
    public static final ResourceLocation REQUEST_PLAYER_SYNC = Utils.ID("request_player_waystone_update");
    public static final ResourceLocation SYNC_PLAYER = Utils.ID("sync_player");
    public static final ResourceLocation SYNC_PLAYER_FROM_CLIENT = Utils.ID("sync_player_from_client");
    public static final ResourceLocation TELEPORT_TO_WAYSTONE = Utils.ID("teleport_to_waystone");
    public static final ResourceLocation TOGGLE_GLOBAL_WAYSTONE = Utils.ID("toggle_global_waystone");
    public static final ResourceLocation VOID_REVIVE = Utils.ID("void_totem_revive");
    public static final ResourceLocation WAYSTONE_GUI_SLOT_CLICK = Utils.ID("waystone_gui_slot_click");
    public static final ResourceLocation WAYSTONE_PACKET = Utils.ID("waystone_packet");
    public static void registerS2CListeners() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WAYSTONE_PACKET, (buffer, context)->{
            Waystones.LOGGER.debug("CLIENT -> SERVER : "+WAYSTONE_PACKET);
            var nbt = buffer.readNbt();
            Player player = context.getPlayer();
            if (Waystones.WAYSTONE_STORAGE == null) {
                Waystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
            }
            Waystones.WAYSTONE_STORAGE.fromTag(nbt);

            if (player == null) {
                return;
            }

            HashSet<String> toForget = new HashSet<>();
            for (String hash : ((PlayerEntityMixinAccess) player).getDiscoveredWaystones()) {
                if (!Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                    toForget.add(hash);
                }
            }
            ((PlayerEntityMixinAccess) player).forgetWaystones(toForget);
            /*TODO: if (player.containerMenu instanceof UniversalWaystoneScreenHandler) {
                ((UniversalWaystoneScreenHandler) player.containerMenu).updateWaystones(player);
                }*/
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SYNC_PLAYER, (buffer, context)->{
            Waystones.LOGGER.debug("CLIENT -> SERVER : "+SYNC_PLAYER);
            CompoundTag tag = buffer.readNbt();
            Player player = context.getPlayer();
            if (player != null) {
                ((PlayerEntityMixinAccess) player).fromTagW(tag);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VOID_REVIVE, (buffer, context)->{
            Waystones.LOGGER.debug("CLIENT -> SERVER : "+VOID_REVIVE);
            Player player = context.getPlayer();
            if (player == null) {
                return;
            }
            //TODO: client.particleManager.addEmitter(player, ParticleTypes.TOTEM_OF_UNDYING, 30);
            player.getLevel().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, player.getSoundSource(), 1.0F, 1.0F);
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack playerStack = player.getInventory().getItem(i);
                if (playerStack.getItem() == ItemReg.VOID_TOTEM.get()) {
                    //TODO: client.gameRenderer.showFloatingItem(playerStack);
                    break;
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static void registerC2SListeners() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REMOVE_WAYSTONE_OWNER, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+REMOVE_WAYSTONE_OWNER);
            Player player = context.getPlayer();
            CompoundTag tag = buffer.readNbt();
            if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            String hash = tag.getString("waystone_hash");
            UUID owner = tag.getUUID("waystone_owner");
            if ((player.getUUID().equals(owner) || player.hasPermissions(2))) {
                Waystones.WAYSTONE_STORAGE.setOwner(hash, null);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WAYSTONE_GUI_SLOT_CLICK, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+WAYSTONE_GUI_SLOT_CLICK);
            CompoundTag tag = buffer.readNbt();
            Player player = context.getPlayer();
            if (tag == null || !tag.contains("sync_id") || !tag.contains("clicked_slot")) {
                return;
            }
            int syncId = tag.getInt("sync_id");
            int button = tag.getInt("clicked_slot");
            if (player.containerMenu.containerId == syncId) {
                player.containerMenu.clickMenuButton(player, button);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RENAME_WAYSTONE, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+RENAME_WAYSTONE);
            CompoundTag tag = buffer.readNbt();
            Player player = context.getPlayer();
            if (tag == null || !tag.contains("waystone_name") || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            String name = tag.getString("waystone_name");
            String hash = tag.getString("waystone_hash");
            UUID owner = tag.getUUID("waystone_owner");
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash) &&
                    ((player.getUUID().equals(owner) &&
                            owner.equals(Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getOwner())) ||
                            player.hasPermissions(2))) {
                Waystones.WAYSTONE_STORAGE.renameWaystone(hash, name);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FORGET_WAYSTONE, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+FORGET_WAYSTONE);
            CompoundTag tag = buffer.readNbt();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            String hash = tag.getString("waystone_hash");
            ((PlayerEntityMixinAccess) context.getPlayer()).forgetWaystone(hash);
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_PLAYER_SYNC, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+REQUEST_PLAYER_SYNC);
            ((PlayerEntityMixinAccess) context.getPlayer()).syncData();
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TOGGLE_GLOBAL_WAYSTONE, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+TOGGLE_GLOBAL_WAYSTONE);
            CompoundTag tag = buffer.readNbt();
            Player player = context.getPlayer();
            if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            var permissionLevel = Waystones.CONFIG.global_mode_toggle_permission_levels;
            UUID owner = tag.getUUID("waystone_owner");
            String hash = tag.getString("waystone_hash");
            switch (permissionLevel) {
                case NONE -> {
                    return;
                }
                case OP -> {
                    if (!player.hasPermissions(2)) {
                        return;
                    }
                }
                case OWNER -> {
                    if (!player.getUUID().equals(owner) || !owner.equals(Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getOwner())) {
                        return;
                    }
                }
            }
            if (Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                Waystones.WAYSTONE_STORAGE.toggleGlobal(hash);
            }
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SYNC_PLAYER_FROM_CLIENT, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+SYNC_PLAYER_FROM_CLIENT);
            ((PlayerEntityMixinAccess) context.getPlayer()).fromTagW(buffer.readNbt());
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TELEPORT_TO_WAYSTONE, (buffer, context)->{
            Waystones.LOGGER.debug("SERVER -> CLIENT : "+TELEPORT_TO_WAYSTONE);
            CompoundTag tag = buffer.readNbt();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            if (!tag.contains("waystone_hash")) {
                return;
            }
            String hash = tag.getString("waystone_hash");
            var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
            if (waystone == null) {
                return;
            }
            if (waystone.getLevel() != null && !(waystone.getLevel().getBlockState(waystone.getBlockPos()).getBlock() instanceof WaystoneBlock)) {
                Waystones.WAYSTONE_STORAGE.removeWaystone(hash);
                waystone.getLevel().removeBlockEntity(waystone.getBlockPos());
            } else {
                waystone.teleportPlayer(context.getPlayer(), true);
            }
        });
    }
}
