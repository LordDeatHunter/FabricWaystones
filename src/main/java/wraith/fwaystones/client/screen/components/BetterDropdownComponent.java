package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.EventStream;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BetterDropdownComponent extends DropdownComponent {

    private Consumer<DismountReason> onDismountHook = reason -> {};

    public BetterDropdownComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    public BetterDropdownComponent onDismount(Consumer<DismountReason> hook) {
        onDismountHook = hook;

        return this;
    }

    @Override
    public void dismount(DismountReason reason) {
        super.dismount(reason);

        onDismountHook.accept(reason);
    }

    public BetterDropdownComponent layout(Consumer<FlowLayout> builder, Consumer<DropdownComponent> onClick) {
        this.entries.child(new ComplexComponent(this, builder, onClick){

        }.margins(Insets.of(2)));

        return this;
    }

    @Override
    public BetterDropdownComponent text(Text text) {
        super.text(text);
        return this;
    }

    @Override
    public BetterDropdownComponent button(Text text, Consumer<DropdownComponent> onClick) {
        super.button(text, onClick);
        return this;
    }

    @Override
    public BetterDropdownComponent checkbox(Text text, boolean state, Consumer<Boolean> onClick) {
        super.checkbox(text, state, onClick);
        return this;
    }

    @Override
    public BetterDropdownComponent nested(Text text, Sizing horizontalSizing, Consumer<DropdownComponent> builder) {
        super.nested(text, horizontalSizing, builder);
        return this;
    }

    protected static class ComplexComponent extends FlowLayout implements ResizeableComponent {

        protected final DropdownComponent parentDropdown;
        protected Consumer<DropdownComponent> onClick;

        public ComplexComponent(DropdownComponent parentDropdown, Consumer<FlowLayout> builder, Consumer<DropdownComponent> onClick) {
            super(Sizing.expand(), Sizing.content(), Algorithm.HORIZONTAL);
            this.parentDropdown = parentDropdown;
            this.onClick = onClick;

            builder.accept(this);

            this.margins(Insets.vertical(1));
            this.cursorStyle(CursorStyle.HAND);
        }

        @Override
        @Nullable
        public Component childAt(int x, int y) {
            return parent();
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public boolean onMouseDown(double mouseX, double mouseY, int button) {
            super.onMouseDown(mouseX, mouseY, button);

            this.onClick.accept(this.parentDropdown);
            this.playInteractionSound();

            return true;
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if (this.isInBoundingBox(mouseX, mouseY)) {
                var margins = this.margins.get();
                context.fill(
                    this.x - margins.left(),
                    this.y - margins.top(),
                    this.x + this.width + margins.right(),
                    this.y + this.height + margins.bottom(),
                    0x44FFFFFF
                );
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }

        protected void playInteractionSound() {
            UISounds.playButtonSound();
        }
    }
}
