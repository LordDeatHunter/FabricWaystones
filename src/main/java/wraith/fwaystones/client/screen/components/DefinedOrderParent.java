package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.container.Containers.*;

public class DefinedOrderParent extends WrappingParentComponent<FlowLayout> {

    private final List<Component> generalComponents = new ArrayList<>();
    private final SequencedMap<String, ComponentBuilder> componentBuilders = new LinkedHashMap<>();

    private final Map<String, Boolean> stateOverrides = new HashMap<>();

    protected DefinedOrderParent(Sizing horizontalSizing, Sizing verticalSizing, boolean isVertical, Consumer<DefinedBuilder> builderFunc) {
        super(Sizing.content(), Sizing.content(), isVertical ? verticalFlow(horizontalSizing, verticalSizing) : horizontalFlow(horizontalSizing, verticalSizing));

        builderFunc.accept(new DefinedBuilder() {
            @Override
            public DefinedBuilder add(String id, Supplier<Component> builder, Supplier<Boolean> condition, boolean allowCaching) {
                componentBuilders.put(id, new DefinedOrderParent.ComponentBuilder(id, builder, condition, allowCaching));

                return this;
            }

            @Override
            public DefinedBuilder add(Component component) {
                generalComponents.add(component);

                return this;
            }
        });

        updateComponents();
    }

    public boolean startAtOrigin = true;

    private DefinedOrderParent orderDirection(boolean startAtOrigin) {
        this.startAtOrigin = startAtOrigin;

        return this;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children());
    }

    public void updateComponentsWithOverrides(Map<String, Boolean> overrides) {
        stateOverrides.clear();
        stateOverrides.putAll(overrides);

        updateComponents();
    }

    private final Map<String, Component> addedComponents = new HashMap<>();

    private record ComponentBuilder(String id, Supplier<Component> builder, Supplier<Boolean> condition, boolean allowCaching) {
        private void attemptComponentBuild(Map<String, Boolean> stateOverrides, Map<String, Component> addedComponents, List<Component> children) {
            if (stateOverrides.getOrDefault(id(), condition().get())) {
                Supplier<Component> componentBuilder = () -> builder().get().id(id());

                var component = allowCaching()
                    ? addedComponents.computeIfAbsent(id(), s -> componentBuilder.get())
                    : componentBuilder.get();

                children.add(component);
                addedComponents.put(id(), component);
            } else {
                addedComponents.remove(id());
            }
        }
    }

    public void updateComponents() {
        List<Component> children = new ArrayList<>(generalComponents);

        for (var value : componentBuilders.values()) {
            value.attemptComponentBuild(stateOverrides, addedComponents, children);
        }

        var layout = this.child();

        if (startAtOrigin) children = children.reversed();

        if (!children.isEmpty() && !layout.children().equals(children)) {
            layout.clearChildren();
            layout.children(children);
        }
    }

    public interface DefinedBuilder {
        default DefinedBuilder add(String id, Supplier<Component> builder, Supplier<Boolean> condition) {
            return add(id, builder, condition, true);
        }

        DefinedBuilder add(String id, Supplier<Component> builder, Supplier<Boolean> condition, boolean allowCaching);

        DefinedBuilder add(Component component);
    }
}
