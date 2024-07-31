package wraith.fwaystones.packets;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.packets.client.SyncPlayerPacket;
import wraith.fwaystones.packets.client.VoidRevivePacket;
import wraith.fwaystones.packets.client.WaystonePacket;
import wraith.fwaystones.util.TeleportSources;

import java.util.UUID;

public final class WaystonePacketHandler {

    private WaystonePacketHandler() {
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(RemoveWaystoneOwnerPacket.PACKET_ID, RemoveWaystoneOwnerPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(WaystoneGUISlotClickPacket.PACKET_ID, WaystoneGUISlotClickPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ForgetWaystonePacket.PACKET_ID, ForgetWaystonePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RenameWaystonePacket.PACKET_ID, RenameWaystonePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestPlayerSyncPacket.PACKET_ID, RequestPlayerSyncPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleGlobalWaystonePacket.PACKET_ID, ToggleGlobalWaystonePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(TeleportToWaystonePacket.PACKET_ID, TeleportToWaystonePacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SyncPlayerFromClientPacket.PACKET_ID, SyncPlayerFromClientPacket.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            registerClientPackets();
    }

    public static void registerClientPackets() {
        PayloadTypeRegistry.playS2C().register(SyncPlayerPacket.PACKET_ID, SyncPlayerPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(VoidRevivePacket.PACKET_ID, VoidRevivePacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(WaystonePacket.PACKET_ID, WaystonePacket.CODEC);
    }
    public static void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(ForgetWaystonePacket.PACKET_ID, WaystonePacketHandler::handleForgetWaystonePacket);
        ServerPlayNetworking.registerGlobalReceiver(RemoveWaystoneOwnerPacket.PACKET_ID, WaystonePacketHandler::handleRemoveWaystoneOwnerPacket);
        ServerPlayNetworking.registerGlobalReceiver(RenameWaystonePacket.PACKET_ID, WaystonePacketHandler::handleRenameWaystonePacket);
        ServerPlayNetworking.registerGlobalReceiver(RequestPlayerSyncPacket.PACKET_ID, WaystonePacketHandler::handleRequestPlayerSyncPacket);
        ServerPlayNetworking.registerGlobalReceiver(SyncPlayerFromClientPacket.PACKET_ID, WaystonePacketHandler::handleSyncPlayerFromClientPacket);
        ServerPlayNetworking.registerGlobalReceiver(TeleportToWaystonePacket.PACKET_ID, WaystonePacketHandler::handleTeleportToWaystonePacket);
        ServerPlayNetworking.registerGlobalReceiver(ToggleGlobalWaystonePacket.PACKET_ID, WaystonePacketHandler::handleToggleGlobalWaystonePacket);
        ServerPlayNetworking.registerGlobalReceiver(WaystoneGUISlotClickPacket.PACKET_ID, WaystonePacketHandler::handleWaystoneGUISlotClickPacket);
    }

    public static void handleForgetWaystonePacket(ForgetWaystonePacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystoneHash())) {
                return;
            }

            context.server().execute(() -> ((PlayerEntityMixinAccess) context.player()).fabricWaystones$forgetWaystone(payload.waystoneHash()));
        });
    }

    public static void handleRemoveWaystoneOwnerPacket(RemoveWaystoneOwnerPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            String hash = payload.waystone();
            UUID owner = payload.owner();
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(hash)) {
                return;
            }
            if ((context.player().getUuid().equals(owner) || context.player().hasPermissionLevel(2))) {
                FabricWaystones.WAYSTONE_STORAGE.setOwner(hash, null);
            }
        });
    }

    public static void handleRenameWaystonePacket(RenameWaystonePacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(payload.waystone()) &&
                    ((context.player().getUuid().equals(payload.owner()) &&
                            payload.owner().equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystone()).getOwner())) ||
                            context.player().hasPermissionLevel(2))) {
                FabricWaystones.WAYSTONE_STORAGE.renameWaystone(payload.waystone(), payload.name());
            }
        });
    }

    public static void handleWaystoneGUISlotClickPacket(WaystoneGUISlotClickPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (context.player().currentScreenHandler.syncId == payload.syncId()) {
                context.player().currentScreenHandler.onButtonClick(context.player(), payload.clickedSlot());
            }
        });
    }

    public static void handleRequestPlayerSyncPacket(RequestPlayerSyncPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(((PlayerEntityMixinAccess) context.player())::fabricWaystones$syncData);
    }

    public static void handleSyncPlayerFromClientPacket(SyncPlayerFromClientPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> ((PlayerEntityMixinAccess) context.player()).fabricWaystones$fromTagW(payload.tag()));
    }

    public static void handleTeleportToWaystonePacket(TeleportToWaystonePacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }

            var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystone());
            if (waystone.getWorld() != null && !(waystone.getWorld().getBlockState(waystone.getPos()).getBlock() instanceof WaystoneBlock)) {
                FabricWaystones.WAYSTONE_STORAGE.removeWaystone(payload.waystone());
                waystone.getWorld().removeBlockEntity(waystone.getPos());
            } else {
                waystone.teleportPlayer(context.player(), true, payload.getSource());
            }
        });
    }

    public static void handleToggleGlobalWaystonePacket(ToggleGlobalWaystonePacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (FabricWaystones.WAYSTONE_STORAGE.removeIfInvalid(payload.waystone())) {
                return;
            }

            var permissionLevel = FabricWaystones.CONFIG.global_mode_toggle_permission_levels();
            switch (permissionLevel) {
                case NONE:
                    return;
                case OP:
                    if (!context.player().hasPermissionLevel(2)) {
                        return;
                    }
                    break;
                case OWNER:
                    if (!context.player().getUuid().equals(payload.owner()) || !payload.owner().equals(FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(payload.waystone()).getOwner())) {
                        return;
                    }
                    break;
            }
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(payload.waystone())) {
                FabricWaystones.WAYSTONE_STORAGE.toggleGlobal(payload.waystone());
            }
        });
    }

}
