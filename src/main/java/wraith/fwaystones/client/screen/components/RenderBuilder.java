package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;

import java.util.function.Function;
import java.util.function.Supplier;

public interface RenderBuilder<T extends Component, R> {

    static <T extends ParentComponent> RenderBuilder<T, Surface> surface() {
        return renderer -> (ctx, component) -> renderer.render(ctx, (T) component);
    }

    static <T extends ButtonComponent> RenderBuilder<T, ButtonComponent.Renderer> button() {
        return renderer -> (ctx, component, delta) -> renderer.render(ctx, (T) component);
    }

    default R texture(Identifier texture, int textureWidth, int textureHeight, MultiStateInteractable<Boolean> holder) {
        var uIncrement = textureWidth / 2;
        var vIncrement = textureHeight / 3;

        return texture(texture, textureWidth, textureHeight, t -> new Vector2i(holder.uIndex() * uIncrement, holder.vIndex() * vIncrement));
    }

    default R texture(Identifier texture, int textureWidth, int textureHeight, Interactable holder) {
        var vIncrement = textureHeight / 3;

        return texture(texture, textureWidth, textureHeight, t -> new Vector2i(0, holder.vIndex() * vIncrement));
    }

    default R texture(Identifier texture, int textureWidth, int textureHeight, Function<T, Vector2i> uvGetter) {
        return this.renderer((ctx, component) -> {
            var uv = uvGetter.apply(component);
            ctx.drawTexture(texture, component.x(), component.y(), uv.x(), uv.y(), component.width(), component.height(), textureWidth, textureHeight);
        });
    }

    default R ninePatched(Identifier activeTexture, Identifier hoveredTexture, Identifier disabledTexture, Interactable holder) {
        return ninePatched(component -> {
            return switch (holder.interact()) {
                case ENABLED -> activeTexture;
                case HOVERED -> hoveredTexture;
                case DISABLED -> disabledTexture;
            };
        });
    }

    default R ninePatched(Function<Integer, Identifier> textureGetter, Supplier<Integer> stateGetter) {
        return ninePatched(t -> textureGetter.apply(stateGetter.get()));
    }

    default R ninePatched(Function<T, Identifier> textureGetter) {
        return renderer((ctx, component) -> NinePatchTexture.draw(textureGetter.apply(component), ctx, component.x(), component.y(), component.width(), component.height()));
    }

    R renderer(Renderer<T> renderer);

    interface Renderer<T extends Component> {
        void render(OwoUIDrawContext ctx, T component);
    }
}
