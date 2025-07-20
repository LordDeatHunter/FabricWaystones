package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.core.Animatable;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import wraith.fwaystones.mixin.owo.TextureComponentAccessor;

import java.util.Objects;
import java.util.function.Function;

public class AdjustableTextureComponent extends TextureComponent {

    protected final AnimatableProperty<AnimatedInteger> uValue;
    protected final AnimatableProperty<AnimatedInteger> vValue;

    protected AdjustableTextureComponent(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        super(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);

        this.uValue = AnimatableProperty.of(new AnimatedInteger(u));
        this.uValue.observe(animatedInteger -> ((TextureComponentAccessor) this).setU(animatedInteger.getValue()));

        this.vValue = AnimatableProperty.of(new AnimatedInteger(v));
        this.vValue.observe(animatedInteger -> ((TextureComponentAccessor) this).setV(animatedInteger.getValue()));
    }

    public static AdjustableTextureComponent of(Identifier texture, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return new AdjustableTextureComponent(texture, 0, 0, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public AdjustableTextureComponent setUV(int u, int v) {
        setU(u);
        setV(v);

        return this;
    }

    public AdjustableTextureComponent setU(int u) {
        uValue.set(new AnimatedInteger(u));

        return this;
    }

    public AnimatableProperty<AnimatedInteger> u() {
        return uValue;
    }

    public AdjustableTextureComponent setV(int v) {
        vValue.set(new AnimatedInteger(v));

        return this;
    }

    public AnimatableProperty<AnimatedInteger> v() {
        return vValue;
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);

        this.uValue.update(delta);
        this.vValue.update(delta);
    }

    public static final class AnimatedInteger extends MutableInt implements Animatable<AnimatedInteger> {

        public AnimatedInteger(int value) {
            super(value);
        }

        @Override
        public AnimatedInteger interpolate(AnimatedInteger next, float delta) {
            return new AnimatedInteger(MathHelper.lerp(delta, this.getValue(), next.getValue()));
        }
    }
}


