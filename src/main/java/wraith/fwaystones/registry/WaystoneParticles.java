package wraith.fwaystones.registry;

import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.particle.RuneParticleEffect;

public final class WaystoneParticles {

    public static final Identifier RUNE_ID = FabricWaystones.id("rune");

    public static final ParticleType<RuneParticleEffect> RUNE = FabricParticleTypes.complex(
        CodecUtils.toMapCodec(RuneParticleEffect.ENDEC),
        CodecUtils.toPacketCodec(RuneParticleEffect.ENDEC)
    );

    public static void init() {
        Registry.register(Registries.PARTICLE_TYPE, RUNE_ID, RUNE);
    }
}
