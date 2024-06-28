package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;


public record WaystoneGUISlotClickPacket(int syncId, int clickedSlot) implements CustomPayload {
    public static final CustomPayload.Id<WaystoneGUISlotClickPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_gui_slot_click"));
    public static final Codec<WaystoneGUISlotClickPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("syncId").forGetter(WaystoneGUISlotClickPacket::syncId),
            Codec.INT.fieldOf("clickedSlot").forGetter(WaystoneGUISlotClickPacket::clickedSlot)
    ).apply(instance, WaystoneGUISlotClickPacket::new));

    public CustomPayload.Id<WaystoneGUISlotClickPacket> getId() {
        return PACKET_ID;
    }

    public static ServerPlayNetworking.PlayPayloadHandler<WaystoneGUISlotClickPacket> getServerPlayHandler() {
        return (payload, context) -> context.server().execute(() -> {
            if (context.player().currentScreenHandler.syncId == payload.syncId()) {
                context.player().currentScreenHandler.onButtonClick(context.player(), payload.clickedSlot());
            }
        });
    }
}