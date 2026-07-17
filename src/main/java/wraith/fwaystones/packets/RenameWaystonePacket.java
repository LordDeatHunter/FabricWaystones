package wraith.fwaystones.packets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import wraith.fwaystones.FabricWaystones;

import java.util.UUID;

public record RenameWaystonePacket(UUID owner, String waystone, String name) implements CustomPacketPayload {
    public static final Type PACKET_ID = new Type<>(Identifier.fromNamespaceAndPath(FabricWaystones.MOD_ID, "rename_waystone"));
    public static final Codec<RenameWaystonePacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.AUTHLIB_CODEC.fieldOf("owner").forGetter(RenameWaystonePacket::owner),
            Codec.STRING.fieldOf("waystone").forGetter(RenameWaystonePacket::waystone),
            Codec.STRING.fieldOf("name").forGetter(RenameWaystonePacket::name)
    ).apply(instance, RenameWaystonePacket::new));
    public static final StreamCodec PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public Type type() {
        return PACKET_ID;
    }
}
