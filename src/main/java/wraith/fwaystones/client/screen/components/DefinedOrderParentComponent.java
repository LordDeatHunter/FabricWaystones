package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.container.Containers.*;

///
/// A Wrapper around a Flowlayout to handle conditional and static components together in a defined order
///
/// Static [Component]s will be added first and then after the [Component]s from the conditional entries
/// will be evaluated and added after.
///
/// The order for both are dictated by the order in which the entries are added with
/// the [addStatic][#addStatic] and [addConditional][#addConditional].
///
public class DefinedOrderParentComponent extends WrappingParentComponent<FlowLayout> {

    private final List<Component> generalComponents = new ArrayList<>();
    private final SequencedMap<String, ComponentBuilder> componentBuilders = new LinkedHashMap<>();

    private boolean startAtOrigin = true;

    private final Map<String, Boolean> stateOverrides = new HashMap<>();

    protected DefinedOrderParentComponent(Sizing horizontalSizing, Sizing verticalSizing, boolean isVertical) {
        super(Sizing.content(), Sizing.content(), isVertical ? verticalFlow(horizontalSizing, verticalSizing) : horizontalFlow(horizontalSizing, verticalSizing));
    }

    public DefinedOrderParentComponent addConditional(String id, Supplier<Component> builder, Supplier<Boolean> condition) {
        return addConditional(id, builder, condition, true);
    }

    public DefinedOrderParentComponent addConditional(String id, Supplier<Component> builder, Supplier<Boolean> condition, boolean allowCaching) {
        componentBuilders.put(id, new ComponentBuilder(id, builder, condition, allowCaching));

        attemptToUpdateComponents();

        return this;
    }

    public DefinedOrderParentComponent addStatic(Component component) {
        generalComponents.add(component);

        attemptToUpdateComponents();

        return this;
    }

    private DefinedOrderParentComponent orderDirection(boolean startAtOrigin) {
        this.startAtOrigin = startAtOrigin;

        attemptToUpdateComponents();

        return this;
    }

    public DefinedOrderParentComponent configureBaseLayout(Consumer<FlowLayout> closure) {
        closure.accept(this.child());

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

        attemptToUpdateComponents();
    }

    @Override
    public void inflate(Size space) {
        updateLayoutChildrenComponents();

        super.inflate(space);
    }

    public void attemptToUpdateComponents() {
        if (this.shouldUpdateComponents) return;

        this.shouldUpdateComponents = true;

        if (this.mounted) this.updateLayout();
    }

    private boolean shouldUpdateComponents = true;

    private final Map<String, Component> addedComponents = new HashMap<>();

    private boolean lock_updateComponents = false;

    private void updateLayoutChildrenComponents() {
        if (!shouldUpdateComponents || lock_updateComponents) return;

        this.shouldUpdateComponents = false;
        this.lock_updateComponents = true;

        List<Component> children = new ArrayList<>(generalComponents);

        for (var value : componentBuilders.values()) {
            if (stateOverrides.getOrDefault(value.id(), value.condition().get())) {
                Supplier<Component> componentBuilder = () -> value.builder().get().id(value.id());

                var component = value.allowCaching()
                    ? addedComponents.computeIfAbsent(value.id(), s -> componentBuilder.get())
                    : componentBuilder.get();

                children.add(component);
                addedComponents.put(value.id(), component);
            } else {
                addedComponents.remove(value.id());
            }
        }

        var layout = this.child();

        if (startAtOrigin) children = children.reversed();

        if (!children.isEmpty() && !layout.children().equals(children)) {
            layout.clearChildren();
            layout.children(children);
        }

        this.lock_updateComponents = false;
    }
}

record ComponentBuilder(String id, Supplier<Component> builder, Supplier<Boolean> condition, boolean allowCaching) {}