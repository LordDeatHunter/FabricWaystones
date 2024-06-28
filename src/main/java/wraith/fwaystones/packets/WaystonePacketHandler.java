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

    public static final Identifier SYNC_PLAYER = Utils.ID("sync_player");
    public static final Identifier VOID_REVIVE = Utils.ID("void_totem_revive");
    public static final Identifier WAYSTONE_PACKET = Utils.ID("waystone_packet");

    private WaystonePacketHandler() {
    }

    public static void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(RemoveWaystoneOwnerPacket.PACKET_ID, RemoveWaystoneOwnerPacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(WaystoneGUISlotClickPacket.PACKET_ID, WaystoneGUISlotClickPacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(ForgetWaystonePacket.PACKET_ID, ForgetWaystonePacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(RenameWaystonePacket.PACKET_ID, RenameWaystonePacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(RequestPlayerSyncPacket.PACKET_ID, RequestPlayerSyncPacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(ToggleGlobalWaystonePacket.PACKET_ID, ToggleGlobalWaystonePacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(TeleportToWaystonePacket.PACKET_ID, TeleportToWaystonePacket.getServerPlayHandler());
        ServerPlayNetworking.registerGlobalReceiver(SyncPlayerFromClientPacket.PACKET_ID, SyncPlayerFromClientPacket.getServerPlayHandler());
    }

}
