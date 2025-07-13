package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.SequencedMap;
import java.util.function.Supplier;

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

    private static void addIfMissing(FlowLayout layout, SequencedMap<String, Supplier<Component>> map) {
        int i = 0;

        for (var entry : map.entrySet()) {
            if (layout.childById(Component.class, entry.getKey()) == null) {
                layout.child(i, entry.getValue().get());
            }
            i++;
        }
    }

    public static void addIfMissing(FlowLayout layout, String id, Supplier<Component> componentSupplier) {
        if (layout.childById(Component.class, id) == null) {
            layout.child(0, componentSupplier.get());
        }
    }

    public static void removeIfPresent(FlowLayout layout, String id) {
        removeIfPresent(layout, id, () -> true);
    }

    private static void removeIfPresent(FlowLayout layout, String id, Supplier<Boolean> extraCheck) {
        var button = layout.childById(Component.class, id);

        if (button != null && extraCheck.get()) {
            layout.removeChild(button);
        }
    }
}
