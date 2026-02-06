package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.component.Components.box;

public class ComponentUtils {
    public static Component wrapNonInteractive(Component component) {
        return new WrappingParentComponent<>(Sizing.content(), Sizing.content(), component) {
            @Override
            @Nullable
            public Component childAt(int x, int y) {
                return parent();
            }

            @Override
            public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
                super.draw(context, mouseX, mouseY, partialTicks, delta);
                this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);
            }
        };
    }

    public static Vector2i getMouseCords() {
        var mouse = MinecraftClient.getInstance().mouse;
        var window = MinecraftClient.getInstance().getWindow();

        int screenX = (int) Math.round(mouse.getX() * window.getScaledWidth() / window.getWidth());
        int screenY = (int) Math.round(mouse.getY() * window.getScaledHeight() / window.getHeight());

        return new Vector2i(screenX, screenY);
    }

    public static DefinedOrderParentComponent createDefinedHolder(Sizing horizontalSizing, Sizing verticalSizing, boolean isVertical) {
        return new DefinedOrderParentComponent(horizontalSizing, verticalSizing, isVertical);
    }

    private static void addIfMissing(Supplier<FlowLayout> layout, SequencedMap<String, Supplier<Component>> map) {
        addIfMissing(layout.get(), map);
    }

    private static void addIfMissing(FlowLayout layout, SequencedMap<String, Supplier<Component>> map) {
        int i = 0;

        for (var entry : map.entrySet()) {
            if (layout.childById(Component.class, entry.getKey()) == null) {
                layout.child(i, entry.getValue().get());
            }
            i++;
        }
    }

    public static void addIfMissing(Supplier<FlowLayout> layout, String id, Supplier<Component> componentSupplier) {
        addIfMissing(layout.get(), id, componentSupplier);
    }

    public static void addIfMissing(FlowLayout layout, String id, Supplier<Component> componentSupplier) {
        if (layout.childById(Component.class, id) == null) {
            layout.child(0, componentSupplier.get());
        }
    }

    public static void removeIfPresent(Supplier<FlowLayout> layout, String id) {
        removeIfPresent(layout.get(), id);
    }

    public static void removeIfPresent(FlowLayout layout, String id) {
        removeIfPresent(layout, id, () -> true);
    }

    private static void removeIfPresent(Supplier<FlowLayout> layout, String id, Supplier<Boolean> extraCheck) {
        removeIfPresent(layout.get(), id, extraCheck);
    }

    private static void removeIfPresent(FlowLayout layout, String id, Supplier<Boolean> extraCheck) {
        var button = layout.childById(Component.class, id);

        if (button != null && extraCheck.get()) {
            layout.removeChild(button);
        }
    }

    public static AdjustableTextureComponent createIcon(Identifier texture){
        return createIcon(texture, true);
    }

    public static AdjustableTextureComponent createIcon(Identifier texture, boolean active){
        return AdjustableTextureComponent.of(texture, 10, 10, 10, 20)
            .setV(active ? 10 : 0);
    }

    public static Component createColoredIcon(int color) {
        return wrapNonInteractive(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                    box(Sizing.fixed(8), Sizing.fixed(8))
                        .fill(true)
                        .color(Color.ofRgb(color))
                )
                .padding(Insets.of(1))
                .surface(Surface.outline(Color.BLACK.interpolate(Color.WHITE, 0.2f).argb()))
        );
    }
}
