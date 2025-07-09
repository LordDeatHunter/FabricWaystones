package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.GlobalPos;
import wraith.fwaystones.particle.effect.RuneParticleEffect;

public record SpawnWaystoneActivationParticles(GlobalPos pos, int color) {
    public static final StructEndec<SpawnWaystoneActivationParticles> ENDEC = StructEndecBuilder.of(
        CodecUtils.toEndec(GlobalPos.CODEC, GlobalPos.PACKET_CODEC).fieldOf("pos", SpawnWaystoneActivationParticles::pos),
        Endec.INT.fieldOf("color", SpawnWaystoneActivationParticles::color),
        SpawnWaystoneActivationParticles::new
    );

    @Environment(EnvType.CLIENT)
    public static void handle(SpawnWaystoneActivationParticles packet, PlayerEntity player) {
        var client = MinecraftClient.getInstance();

        var world = client.world;
        if (world == null) return;

        if (world.getRegistryKey() != packet.pos.dimension()) return;

        var pos = packet.pos.pos();

        if (!player.getPos().isInRange(pos.up().toBottomCenterPos(), 64)) return;

        var particle = new RuneParticleEffect(packet.color);
        for (int i = 0; i < 50; i++) {
            var speed = 2;
            world.addParticle(
                particle,
                pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 2,
                pos.getY() + 0.1 + (world.random.nextDouble() - 0.1) + speed,
                pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 2,
                0, -speed, 0
            );
        }
    }
}
