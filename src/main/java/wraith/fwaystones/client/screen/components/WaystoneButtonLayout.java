package wraith.fwaystones.client.screen.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.client.screen.ExperimentalWaystoneScreen;

import java.util.UUID;

import static io.wispforest.owo.ui.component.Components.label;
import static wraith.fwaystones.client.screen.components.ComponentUtils.*;

public class WaystoneButtonLayout extends FlowLayout {

    private static final Identifier ACTIVE_TEXTURE = FabricWaystones.id("waystone_button/active");
    private static final Identifier HOVERED_TEXTURE = FabricWaystones.id("waystone_button/hovered");
    private static final Identifier DISABLED_TEXTURE = FabricWaystones.id("waystone_button/disabled");

    private final Interactable buttonState;

    public WaystoneButtonLayout(ExperimentalWaystoneScreen screen, Sizing horizontalSizing, Sizing verticalSizing, UUID uuid) {
        super(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);

        var data = screen.storage.getDataIfTypeOrThrow(uuid, NetworkedWaystoneData.class);
        var existsWithinWorld = screen.storage.getPosition(uuid) != null;

        this.buttonState = new Interactable.Mutable(existsWithinWorld) {
            @Override
            public InteractionType interact() {
                var cords = getMouseCords();
                var layout = WaystoneButtonLayout.this;

                var hoveringTarget = layout.childAt(cords.x(), cords.y());

                var interactionType = super.interact();

                if (screen.selectingUUIDs) {
                    if (screen.selectedUUIDs.contains(uuid)) {
                        interactionType = InteractionType.HOVERED;
                    } else {
                        interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                    }

                    return interactionType;
                }

                if (hoveringTarget != null) {
                    if (layout.children().contains(hoveringTarget)) {
                        interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                    } else if(hoveringTarget.parent() instanceof ParentComponent parentHover && layout.children().contains(parentHover)) {
                        interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                    }
                }

                return interactionType;
            }
        };

        if (existsWithinWorld) {
            this.mouseEnter().subscribe(() -> buttonState.interact(true));
            this.mouseLeave().subscribe(() -> buttonState.interact(false));

            this.mouseUp().subscribe((mouseX, mouseY, button) -> {
                if (!buttonState.interact().equals(Interactable.InteractionType.HOVERED)) return false;

                if (button == 0) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                    screen.getScreenHandler().attemptTeleport(uuid);

                    return true;
                } else if (button == 1) {
                    screen.selectingUUIDs = true;
                    screen.selectedUUIDs.add(uuid);

                    screen.addOverlay(uuid);

                    return true;
                }

                return false;
            });
        }

        //--

        this.surface(
            RenderBuilder.surface()
                .ninePatched(ACTIVE_TEXTURE, HOVERED_TEXTURE, DISABLED_TEXTURE, buttonState)
        );

        //--

        this.child(
                wrapNonInteractive(
                    label(data.parsedName())
                        .margins(Insets.left(2))
                )
            )
            .child(
                createDefinedHolder(Sizing.content(), Sizing.content(), false)
                    .addStatic(
                        Components.button(Text.empty(), btn -> screen.setupSettingsWindow(uuid))
                            .renderer(ButtonComponent.Renderer.texture(FabricWaystones.gui("settings_icon"), 0, 0, 12, 12))
                            .sizing(Sizing.fixed(12))
                    )
                    .addConditional("color_icon", () -> createColoredIcon(screen.storage.getData(uuid).color()), () -> !data.isDefaultColor())
                    .addConditional("global_icon", () -> createIcon(ExperimentalWaystoneScreen.GLOBAL_ICON), () -> screen.storage.isGlobal(uuid))
                    .addConditional("favorite_icon", () -> createIcon(ExperimentalWaystoneScreen.FAVORITE_ICON), () -> screen.playerData.isFavorited(uuid))
                    .configureBaseLayout(layout -> {
                        layout.gap(2)
                            .verticalAlignment(VerticalAlignment.CENTER)
                            .padding(Insets.of(2));
                    })
                    .positioning(Positioning.relative(100, 50))
                    .id("button_layout")
            );
        }
}
