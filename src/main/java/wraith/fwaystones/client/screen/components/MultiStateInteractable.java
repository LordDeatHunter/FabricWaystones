package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.ButtonComponent;

public interface MultiStateInteractable<T> extends Interactable {

    static MultiStateInteractable<Boolean> toggle(boolean toggled, boolean enabled) {
        return toggle(toggled, enabled ? InteractionType.ENABLED : InteractionType.DISABLED);
    }

    static MultiStateInteractable<Boolean> toggle(boolean toggled, InteractionType startingType) {
        return new Toggle(toggled) {
            private InteractionType type = startingType;

            @Override
            public void interact(InteractionType type) { this.type = type; }

            @Override
            public InteractionType interact() { return this.type; }
        };
    }

    static Interactable from(boolean toggled, ButtonComponent component) {
        var wrapper = Interactable.from(component);

        return new Toggle(toggled) {
            @Override
            public void interact(InteractionType type) { wrapper.interact(type); }

            @Override
            public InteractionType interact() { return wrapper.interact(); }
        };
    }

    void state(T value);

    T state();

    int uIndex();
}

abstract class Toggle implements MultiStateInteractable<Boolean> {

    private boolean toggled;

    Toggle(boolean toggled) {
        this.toggled = toggled;
    }

    @Override
    public void state(Boolean value) {
        this.toggled = value;
    }

    @Override
    public Boolean state() {
        return this.toggled;
    }

    @Override
    public int uIndex() {
        return this.toggled ? 0 : 1;
    }
}
