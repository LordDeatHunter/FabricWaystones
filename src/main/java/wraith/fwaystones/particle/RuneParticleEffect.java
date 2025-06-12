package wraith.fwaystones.particle;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ConnectionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.jetbrains.annotations.NotNull;
import wraith.fwaystones.registry.WaystoneParticles;

import static wraith.fwaystones.util.Utils.random;

public record RuneParticleEffect(int color) implements ParticleEffect {

    public static final StructEndec<RuneParticleEffect> ENDEC = StructEndecBuilder.of(
        Endec.INT.fieldOf("color", RuneParticleEffect::color),
        RuneParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return WaystoneParticles.RUNE;
    }

    @Environment(EnvType.CLIENT)
    public record Factory(SpriteProvider spriteProvider) implements ParticleFactory<RuneParticleEffect> {
        @Override
        public @NotNull Particle createParticle(
            RuneParticleEffect parameters,
            ClientWorld world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ
        ) {
            ConnectionParticle connectionParticle = new ConnectionParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            connectionParticle.setSprite(this.spriteProvider);
            var color = Color.ofRgb(parameters.color);
            var shade = random.nextFloat(0.85f, 1f);
            connectionParticle.setColor(color.red() * shade, color.green() * shade, color.blue() * shade);
            connectionParticle.scale(1.25f);
            connectionParticle.setMaxAge(random.nextInt(20, 50));
            return connectionParticle;
        }
    }
}
