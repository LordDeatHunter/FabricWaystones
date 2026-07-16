package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wraith.fwaystones.FabricWaystones;


public record WaystoneGUISlotClickPacket(int syncId, int clickedSlot) implements CustomPacketPayload {
    public static final Type PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, "waystone_gui_slot_click"));
    public static final Codec<WaystoneGUISlotClickPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("syncId").forGetter(WaystoneGUISlotClickPacket::syncId),
            Codec.INT.fieldOf("clickedSlot").forGetter(WaystoneGUISlotClickPacket::clickedSlot)
    ).apply(instance, WaystoneGUISlotClickPacket::new));
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Type type() {
        return PACKET_ID;
    }
}