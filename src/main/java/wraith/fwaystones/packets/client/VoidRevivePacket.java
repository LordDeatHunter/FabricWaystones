package wraith.fwaystones.packets.client;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.registry.ItemRegistry;

public record VoidRevivePacket(NbtCompound tag) implements CustomPayload{
    public static final Id<VoidRevivePacket> PACKET_ID = new Id<>(Identifier.of(FabricWaystones.MOD_ID, "void_totem_revive"));
    private static final VoidRevivePacket INSTANCE = new VoidRevivePacket(new NbtCompound());
    public static final Codec<VoidRevivePacket> CODEC = Codec.unit(INSTANCE);

    public Id<VoidRevivePacket> getId() {
        return PACKET_ID;
    }

    public static ClientPlayNetworking.PlayPayloadHandler<VoidRevivePacket> getClientPlayHandler() {
        return (payload, context) -> {
            var client = context.client();
            client.execute(() -> {
                if (client.player != null) {
                    client.particleManager.addEmitter(client.player, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    context.player().getWorld().playSound(client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ITEM_TOTEM_USE, client.player.getSoundCategory(), 1.0F, 1.0F, false);
                    for (int i = 0; i < client.player.getInventory().size(); ++i) {
                        ItemStack playerStack = client.player.getInventory().getStack(i);
                        if (playerStack.getItem() == ItemRegistry.get("void_totem")) {
                            client.gameRenderer.showFloatingItem(playerStack);
                            break;
                        }
                    }
                }
            });
        };
    }
}
