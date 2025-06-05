package wraith.fwaystones.mixin.scoreboard;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentState.class)
public abstract class PersistentStateMixin {

    @ModifyReturnValue(method = "isDirty", at = @At("RETURN"))
    private boolean wraithWaystones$forceDirty(boolean original) {
        // TODO: THIS SEEMS QUITE HACKY
        return (!original && (((Object)(this)) instanceof ScoreboardState));
    }
}
