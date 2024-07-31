package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;


public record WaystoneGUISlotClickPacket(int syncId, int clickedSlot) implements CustomPayload {
    public static final Id PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "waystone_gui_slot_click"));
    public static final Codec<WaystoneGUISlotClickPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("syncId").forGetter(WaystoneGUISlotClickPacket::syncId),
            Codec.INT.fieldOf("clickedSlot").forGetter(WaystoneGUISlotClickPacket::clickedSlot)
    ).apply(instance, WaystoneGUISlotClickPacket::new));
    public static final PacketCodec PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public Id getId() {
        return PACKET_ID;
    }
}