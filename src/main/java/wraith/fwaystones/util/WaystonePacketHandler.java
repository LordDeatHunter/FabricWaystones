package wraith.fwaystones.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;

import java.util.UUID;

public final class WaystonePacketHandler {

    public static final Identifier FORGET_WAYSTONE = Utils.ID("forget_waystone");
    public static final Identifier REMOVE_WAYSTONE_OWNER = Utils.ID("remove_waystone_owner");
    public static final Identifier RENAME_WAYSTONE = Utils.ID("rename_waystone");
    public static final Identifier REQUEST_PLAYER_SYNC = Utils.ID("request_player_waystone_update");
    public static final Identifier SYNC_PLAYER = Utils.ID("sync_player");
    public static final Identifier SYNC_PLAYER_FROM_CLIENT = Utils.ID("sync_player_from_client");
    public static final Identifier TELEPORT_TO_WAYSTONE = Utils.ID("teleport_to_waystone");
    public static final Identifier TOGGLE_GLOBAL_WAYSTONE = Utils.ID("toggle_global_waystone");
    public static final Identifier VOID_REVIVE = Utils.ID("void_totem_revive");
    public static final Identifier WAYSTONE_GUI_SLOT_CLICK = Utils.ID("waystone_gui_slot_click");
    public static final Identifier WAYSTONE_PACKET = Utils.ID("waystone_packet");

    private WaystonePacketHandler() {
    }

    public static void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_WAYSTONE_OWNER, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            server.execute(() -> {
                if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                UUID owner = tag.getUuid("waystone_owner");
                if ((player.getUuid().equals(owner) || player.hasPermissionLevel(2))) {
                    FabricWaystones.WAYSTONE_STORAGE.setOwner(hash, null);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(WAYSTONE_GUI_SLOT_CLICK, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            server.execute(() -> {
                if (tag == null || !tag.contains("sync_id") || !tag.contains("clicked_slot")) {
                    return;
                }
                int syncId = tag.getInt("sync_id");
                int button = tag.getInt("clicked_slot");
                if (player.currentScreenHandler.syncId == syncId) {
                    player.currentScreenHandler.onButtonClick(player, button);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(RENAME_WAYSTONE, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_name") || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            String name = tag.getString("waystone_name");
            String hash = tag.getString("waystone_hash");
            UUID owner = tag.getUuid("waystone_owner");
            server.execute(() -> {
                if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash) &&
                    ((player.getUuid().equals(owner) &&
                        owner.equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getOwner())) ||
                        player.hasPermissionLevel(2))) {
                    FabricWaystones.WAYSTONE_STORAGE.renameWaystone(hash, name);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(FORGET_WAYSTONE, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            String hash = tag.getString("waystone_hash");
            server.execute(() -> ((PlayerEntityMixinAccess) player).forgetWaystone(hash));
        });
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_PLAYER_SYNC, (server, player, networkHandler, data, sender) ->
            server.execute(((PlayerEntityMixinAccess) player)::syncData)
        );
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_GLOBAL_WAYSTONE, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            server.execute(() -> {
                var permissionLevel = FabricWaystones.CONFIG.global_mode_toggle_permission_levels();
                UUID owner = tag.getUuid("waystone_owner");
                String hash = tag.getString("waystone_hash");
                switch (permissionLevel) {
                    case NONE:
                        return;
                    case OP:
                        if (!player.hasPermissionLevel(2)) {
                            return;
                        }
                        break;
                    case OWNER:
                        if (!player.getUuid().equals(owner) || !owner.equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash).getOwner())) {
                            return;
                        }
                        break;
                }
                if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                    FabricWaystones.WAYSTONE_STORAGE.toggleGlobal(hash);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SYNC_PLAYER_FROM_CLIENT, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            server.execute(() -> ((PlayerEntityMixinAccess) player).fromTagW(tag));
        });
        ServerPlayNetworking.registerGlobalReceiver(TELEPORT_TO_WAYSTONE, (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            server.execute(() -> {
                if (!tag.contains("waystone_hash")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone == null) {
                    return;
                }
                if (waystone.getWorld() != null && !(waystone.getWorld().getBlockState(waystone.getPos()).getBlock() instanceof WaystoneBlock)) {
                    FabricWaystones.WAYSTONE_STORAGE.removeWaystone(hash);
                    waystone.getWorld().removeBlockEntity(waystone.getPos());
                } else {
                    waystone.teleportPlayer(player, true);
                }
            });
        });
    }

}
