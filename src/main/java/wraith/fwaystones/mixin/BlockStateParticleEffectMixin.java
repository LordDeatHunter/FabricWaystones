package wraith.fwaystones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import wraith.fwaystones.pond.BlockStateParticleEffectExtension;

import java.util.Optional;

@Mixin(BlockStateParticleEffect.class)
public abstract class BlockStateParticleEffectMixin implements BlockStateParticleEffectExtension {

    @Unique
    public BlockPos truePos = null;

    @Override
    public void fwaystones$setTruePos(BlockPos pos) {
        truePos = pos;
    }

    @Override
    public BlockPos fwaystones$getTruePos() {
        return truePos;
    }

    @WrapMethod(method = "createPacketCodec")
    private static PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect> fwaystones$addOptionalBlockPos(ParticleType<BlockStateParticleEffect> type, Operation<PacketCodec<? super RegistryByteBuf, BlockStateParticleEffect>> original) {
        var codec = original.call(type);

        return PacketCodec.tuple(
            codec,
            effect -> effect,
            PacketCodecs.optional(BlockPos.PACKET_CODEC),
            effect -> Optional.ofNullable(((BlockStateParticleEffectExtension) effect).fwaystones$getTruePos()),
            (effect, pos) -> {
                ((BlockStateParticleEffectExtension) effect).fwaystones$setTruePos(pos.orElse(null));

                return effect;
            }
        );
    }
}
