package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import wraith.fwaystones.pond.MapStateDuck;

import java.util.List;
import java.util.UUID;

public record UpdateMapStateWaystoneMarkers(MapIdComponent mapId, List<UUID> markers) {
    public static final StructEndec<UpdateMapStateWaystoneMarkers> ENDEC = StructEndecBuilder.of(
        CodecUtils.toEndec(MapIdComponent.CODEC, MapIdComponent.PACKET_CODEC).fieldOf("mapId", UpdateMapStateWaystoneMarkers::mapId),
        BuiltInEndecs.UUID.listOf().fieldOf("markers", UpdateMapStateWaystoneMarkers::markers),
        UpdateMapStateWaystoneMarkers::new
    );

    @Environment(EnvType.CLIENT)
    public static void handle(UpdateMapStateWaystoneMarkers packet, PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        var world = client.world;
        if (world == null) return;
        MapState mapState = client.world.getMapState(packet.mapId);
        if (mapState == null) return;
        ((MapStateDuck) mapState).fwaystones$setWaystoneMarkers(packet.markers());
    }
}
