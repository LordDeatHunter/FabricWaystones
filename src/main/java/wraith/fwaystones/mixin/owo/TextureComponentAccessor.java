package wraith.fwaystones.mixin.owo;

import io.wispforest.owo.ui.component.TextureComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureComponent.class)
public interface TextureComponentAccessor {
    @Accessor("u")
    @Mutable
    void setU(int u);
    @Accessor("v")
    @Mutable
    void setV(int v);
}
