package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record RemoveWaystoneOwnerPacket(UUID owner, String waystone) implements CustomPacketPayload {
    public static final Type PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath(FabricWaystones.MOD_ID, "remove_waystone_owner"));
    public static final Codec<RemoveWaystoneOwnerPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.AUTHLIB_CODEC.fieldOf("owner").forGetter(RemoveWaystoneOwnerPacket::owner),
            Codec.STRING.fieldOf("waystone").forGetter(RemoveWaystoneOwnerPacket::waystone)
    ).apply(instance, RemoveWaystoneOwnerPacket::new));
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Type type() {
        return PACKET_ID;
    }
}