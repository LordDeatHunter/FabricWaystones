package wraith.fwaystones.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface ExhaustionAccessor {

    @Accessor("exhaustionLevel")
    float getExhaustion();
}
