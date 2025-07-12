package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.ButtonComponent;

public interface Interactable {

    static Interactable mutable(boolean enabled) {
        return new Mutable(enabled);
    }

    static Interactable mutable(InteractionType type) {
        return new Mutable(type);
    }

    static Interactable from(ButtonComponent component) {
        return new Interactable() {
            @Override
            public void interact(InteractionType type) {
                switch (type) {
                    case ENABLED -> component.active = true;
                    case DISABLED -> component.active = false;
                }
            }

            @Override
            public InteractionType interact() {
                return component.active ? (component.isHovered() ? InteractionType.HOVERED : InteractionType.ENABLED) : InteractionType.DISABLED;
            }
        };
    }

    void interact(InteractionType type);

    default void interact(boolean hovered) {
        if (this.interact().equals(InteractionType.DISABLED)) return;

        interact(hovered ? InteractionType.HOVERED : InteractionType.ENABLED);
    }

    InteractionType interact();

    default int vIndex() {
        return interact().ordinal() - 1;
    }

    enum InteractionType {
        ENABLED,
        HOVERED,
        DISABLED;
    }

    class Mutable implements Interactable {

        private InteractionType type;

        public Mutable(boolean enabled) {
            this(enabled ? InteractionType.ENABLED : InteractionType.DISABLED);
        }

        public Mutable(InteractionType type) {
            this.type = type;
        }

        @Override
        public void interact(InteractionType type) {
            this.type = type;
        }
        @Override
        public InteractionType interact() {
            return this.type;
        }
    }
}


