package wraith.waystones.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;

import java.util.UUID;

public final class WaystonePacketHandler {

    private WaystonePacketHandler() {}

    public static void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("remove_waystone_owner"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            server.execute(() -> {
                if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                UUID owner = tag.getUuid("waystone_owner");
                if (Waystones.WAYSTONE_STORAGE.containsHash(hash) && ((player.getUuid().equals(owner) &&
                        Waystones.WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) || player.hasPermissionLevel(2))) {
                    Waystones.WAYSTONE_STORAGE.setOwner(hash, null);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_gui_slot_click"), (server, player, networkHandler, data, sender) -> {
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
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("rename_waystone"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_name") || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            String name = tag.getString("waystone_name");
            String hash = tag.getString("waystone_hash");
            UUID owner = tag.getUuid("waystone_owner");
            server.execute(() -> {
                if (Waystones.WAYSTONE_STORAGE.containsHash(hash) &&
                        ((player.getUuid().equals(owner) &&
                                Waystones.WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) ||
                                player.hasPermissionLevel(2))) {
                    Waystones.WAYSTONE_STORAGE.renameWaystone(hash, name);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("forget_waystone"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();

            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }

            String hash = tag.getString("waystone_hash");
            server.execute(() -> {
                var waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone == null || waystone.isGlobal()) {
                    return;
                }
                ((PlayerEntityMixinAccess) player).forgetWaystone(hash);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("request_player_waystone_update"), (server, player, networkHandler, data, sender) -> server.execute(((PlayerEntityMixinAccess) player)::syncData));
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("toggle_global_waystone"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            server.execute(() -> {
                String hash = tag.getString("waystone_hash");
                UUID owner = tag.getUuid("waystone_owner");
                if (Waystones.WAYSTONE_STORAGE.containsHash(hash) &&
                        ((player.getUuid().equals(owner) &&
                                Waystones.WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) ||
                                player.hasPermissionLevel(2))) {
                    Waystones.WAYSTONE_STORAGE.toggleGlobal(hash);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("sync_player_from_client"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            server.execute(() -> ((PlayerEntityMixinAccess) player).fromTagW(tag));
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("teleport_to_waystone"), (server, player, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            server.execute(() -> {
                if (!tag.contains("waystone_hash")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                boolean isAbyssWatcher = tag.contains("from_abyss_watcher") && tag.getBoolean("from_abyss_watcher");
                var waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone == null) {
                    return;
                }
                if (waystone.getWorld() != null && !(waystone.getWorld().getBlockState(waystone.getPos()).getBlock() instanceof WaystoneBlock)) {
                    Waystones.WAYSTONE_STORAGE.removeWaystone(hash);
                    waystone.getWorld().removeBlockEntity(waystone.getPos());
                } else if (Utils.canTeleport(player, hash)) {
                    BlockPos playerPos = player.getBlockPos();
                    waystone.teleportPlayer(player, isAbyssWatcher);

                    player.world.playSound(player, playerPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
                    if (isAbyssWatcher) {
                        player.world.playSound(player, playerPos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1F, 1F);
                    }

                    BlockPos waystonePos = waystone.getPos();
                    if (!waystonePos.isWithinDistance(playerPos, 6)) {
                        player.world.playSound(player, waystonePos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
                    }
                }
            });
        });
    }

}
