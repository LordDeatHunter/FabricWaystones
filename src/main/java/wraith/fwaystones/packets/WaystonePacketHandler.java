package wraith.fwaystones.packets;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.util.Utils;

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
        ServerPlayNetworking.registerGlobalReceiver(RemoveWaystoneOwnerPacket.PACKET_ID, RemoveWaystoneOwnerPacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(WaystoneGUISlotClickPacket.PACKET_ID, WaystoneGUISlotClickPacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(ForgetWaystonePacket.PACKET_ID, ForgetWaystonePacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(RenameWaystonePacket.PACKET_ID, RenameWaystonePacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(RequestPlayerSyncPacket.PACKET_ID, RequestPlayerSyncPacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(ToggleGlobalWaystonePacket.PACKET_ID, ToggleGlobalWaystonePacket.getPlayPayloadHandler());
        ServerPlayNetworking.registerGlobalReceiver(TeleportToWaystonePacket.PACKET_ID, TeleportToWaystonePacket.getPlayPayloadHandler());

        // TODO: Make this packet sync the player data from the client.
//        ServerPlayNetworking.registerGlobalReceiver(SYNC_PLAYER_FROM_CLIENT, (payload, context) -> {
//            NbtCompound tag = payload.readNbt();
//            context.server().execute(() -> ((PlayerEntityMixinAccess) context.player()).fabricWaystones$fromTagW(tag));
//        });
    }

}
