package wraith.fwaystones.registry;

import io.wispforest.endec.StructEndec;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.particle.effect.RuneParticleEffect;

public final class WaystoneParticles {

    public static final ParticleType<RuneParticleEffect> RUNE = registerComplex("rune", RuneParticleEffect.ENDEC);
    public static final SimpleParticleType HELIX = registerSimple("helical_portal");

    public static void init() {}

    private static <T extends ParticleEffect> ParticleType<T> registerComplex(String id, StructEndec<T> endec) {
        return Registry.register(
            Registries.PARTICLE_TYPE,
            FabricWaystones.id(id),
            FabricParticleTypes.complex(
                CodecUtils.toMapCodec(endec),
                CodecUtils.toPacketCodec(endec)
            )
        );
    }

    private static SimpleParticleType registerSimple(String id) {
        return Registry.register(
            Registries.PARTICLE_TYPE,
            FabricWaystones.id(id),
            FabricParticleTypes.simple()
        );
    }
}
