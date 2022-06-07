package wraith.fwaystones.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SinglePoolElement.class)
public interface SinglePoolElementAccessor {

    @Accessor("location")
    Either<Identifier, Structure> getLocation();
}
