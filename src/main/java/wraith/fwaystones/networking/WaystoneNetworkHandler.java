package wraith.fwaystones.networking;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.networking.packets.SyncWaystonePlayerDataChange;
import wraith.fwaystones.networking.packets.c2s.*;
import wraith.fwaystones.networking.packets.s2c.*;

import java.util.function.BiConsumer;

public class WaystoneNetworkHandler {
    public static final OwoNetChannel CHANNEL = OwoNetChannel.create(FabricWaystones.id("main"));

    public static void init() {
        CHANNEL.registerServerbound(TeleportToWaystone.class, TeleportToWaystone.ENDEC, handler(TeleportToWaystone::handle));
        CHANNEL.registerServerbound(ToggleGlobalWaystone.class, ToggleGlobalWaystone.ENDEC, handler(ToggleGlobalWaystone::handle));
        CHANNEL.registerServerbound(RevokeWaystoneOwner.class, RevokeWaystoneOwner.ENDEC, handler(RevokeWaystoneOwner::handle));
        CHANNEL.registerServerbound(RenameWaystone.class, RenameWaystone.ENDEC, handler(RenameWaystone::handle));
        CHANNEL.registerServerbound(WaystoneGUISlotClick.class, WaystoneGUISlotClick.ENDEC, handler(WaystoneGUISlotClick::handle));

        CHANNEL.registerServerbound(SyncWaystonePlayerDataChange.class, SyncWaystonePlayerDataChange.ENDEC, handler(SyncWaystonePlayerDataChange::handle));
        CHANNEL.registerServerbound(AttemptTeleporterUse.class, AttemptTeleporterUse.ENDEC, handler(AttemptTeleporterUse::handle));

        //--

        CHANNEL.registerClientboundDeferred(SyncWaystonePlayerDataChange.class, SyncWaystonePlayerDataChange.ENDEC);

        CHANNEL.registerClientboundDeferred(SyncWaystonePlayerData.class, SyncWaystonePlayerData.ENDEC);
        CHANNEL.registerClientboundDeferred(SyncWaystoneDataChange.class, SyncWaystoneDataChange.ENDEC);
        CHANNEL.registerClientboundDeferred(SyncWaystoneDataChanges.class, SyncWaystoneDataChanges.ENDEC);
        CHANNEL.registerClientboundDeferred(SyncWaystonePositionChange.class, SyncWaystonePositionChange.ENDEC);
        CHANNEL.registerClientboundDeferred(SyncWaystonePositionChanges.class, SyncWaystonePositionChanges.ENDEC);
        CHANNEL.registerClientboundDeferred(SyncWaystoneStorage.class, SyncWaystoneStorage.ENDEC);

        CHANNEL.registerClientboundDeferred(VoidRevive.class, VoidRevive.ENDEC);

        CHANNEL.registerClientboundDeferred(UpdateMapStateWaystoneMarkers.class, UpdateMapStateWaystoneMarkers.ENDEC);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        CHANNEL.registerClientbound(SyncWaystonePlayerDataChange.class, SyncWaystonePlayerDataChange.ENDEC, handler(SyncWaystonePlayerDataChange::handle));

        CHANNEL.registerClientbound(SyncWaystonePlayerData.class, SyncWaystonePlayerData.ENDEC, handler(SyncWaystonePlayerData::handle));
        CHANNEL.registerClientbound(SyncWaystoneDataChange.class, SyncWaystoneDataChange.ENDEC, handler(SyncWaystoneDataChange::handle));
        CHANNEL.registerClientbound(SyncWaystoneDataChanges.class, SyncWaystoneDataChanges.ENDEC, handler(SyncWaystoneDataChanges::handle));
        CHANNEL.registerClientbound(SyncWaystonePositionChange.class, SyncWaystonePositionChange.ENDEC, handler(SyncWaystonePositionChange::handle));
        CHANNEL.registerClientbound(SyncWaystonePositionChanges.class, SyncWaystonePositionChanges.ENDEC, handler(SyncWaystonePositionChanges::handle));
        CHANNEL.registerClientbound(SyncWaystoneStorage.class, SyncWaystoneStorage.ENDEC, handler(SyncWaystoneStorage::handle));

        CHANNEL.registerClientbound(VoidRevive.class, VoidRevive.ENDEC, handler(VoidRevive::handle));

        CHANNEL.registerClientbound(UpdateMapStateWaystoneMarkers.class, UpdateMapStateWaystoneMarkers.ENDEC, handler(UpdateMapStateWaystoneMarkers::handle));
    }

    public static <P extends PlayerEntity, R, N, A extends OwoNetChannel.EnvironmentAccess<P, R, N>, M extends Record> OwoNetChannel.ChannelHandler<M, A> handler(BiConsumer<M, PlayerEntity> handler) {
        return (m, a) -> handler.accept(m, a.player());
    }
}
