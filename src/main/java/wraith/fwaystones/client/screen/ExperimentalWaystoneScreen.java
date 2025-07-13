package wraith.fwaystones.client.screen;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import io.wispforest.owo.ui.util.ScissorStack;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.client.screen.components.BetterDropdownComponent;
import wraith.fwaystones.client.screen.components.Interactable;
import wraith.fwaystones.client.screen.components.RenderBuilder;

import java.util.*;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.container.Containers.*;
import static wraith.fwaystones.client.screen.components.ComponentUtils.*;
import static io.wispforest.owo.ui.component.Components.*;

public class ExperimentalWaystoneScreen extends BaseOwoHandledScreen<FlowLayout, ExperimentalWaystoneScreenHandler> {

    public static final Identifier ACTIVE_TEXTURE = FabricWaystones.id("waystone_button/active");
    public static final Identifier HOVERED_TEXTURE = FabricWaystones.id("waystone_button/hovered");
    public static final Identifier DISABLED_TEXTURE = FabricWaystones.id("waystone_button/disabled");

    public static final Identifier FAVORITE_ICON = FabricWaystones.id("textures/gui/favorite_icon.png");

    private final WaystoneDataStorage storage;
    private final WaystonePlayerData playerData;

    private ScrollContainer<FlowLayout> waystoneList;

    public ExperimentalWaystoneScreen(ExperimentalWaystoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.playerInventoryTitleX = 42069;
        this.storage = WaystoneDataStorage.getStorage(MinecraftClient.getInstance());
        playerData = WaystonePlayerData.getData(MinecraftClient.getInstance().player);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    private List<UUID> getSortedWaystones(String searchText) {
        var sortedWaystones = playerData.discoveredWaystones().stream()
            .map(uuid -> storage.getData(uuid) instanceof NetworkedWaystoneData networkedData ? networkedData : null)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(NetworkedWaystoneData::sortingName))
            .toList();

        if (searchText.isBlank()) {
            return sortedWaystones.stream()
                .map(WaystoneData::uuid)
                .sorted((uuid1, uuid2) -> Boolean.compare(storage.hasPosition(uuid2), storage.hasPosition(uuid1)))
                .sorted((o1, o2) -> Boolean.compare(playerData.isFavorited(o2), playerData.isFavorited(o1)))
                .toList();
        } else {
            return FuzzySearch.extractSorted(searchText.toLowerCase(Locale.ROOT), sortedWaystones, NetworkedWaystoneData::sortingName, 75)
                .stream()
                .map(result -> result.getReferent().uuid())
                .toList();
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var columnWidth = 140;
        // TODO: ADD EVENT HOOKS FOR EITHER DATA CHANGES OR DISCOVERY OR WAYSTONE REPOSITINONED
        waystoneList = verticalScroll(
            Sizing.fixed(columnWidth), Sizing.fixed(columnWidth + 20),
            createWaystoneList("")
        );

        rootComponent.child(
                horizontalFlow(Sizing.content(), Sizing.content())
                    .child(
                        verticalFlow(Sizing.content(), Sizing.content())
                            .id("left_panel_holder")
                    )
                    .child(
                        verticalFlow(Sizing.content(), Sizing.content())
                            .child(
                                textBox(Sizing.fixed(columnWidth), "")
                                    .<TextBoxComponent>configure(textBox -> textBox.onChanged()
                                        .subscribe(this::setWaystoneList))
                                    .id("search_box")
                                    .margins(Insets.of(-1))
                            )
                            .child(waystoneList
                                .surface(Surface.panelWithInset(0))
                                .padding(Insets.of(1))
                                .allowOverflow(false))
                            .gap(4)
                            .allowOverflow(false)
                            .padding(Insets.of(7))
                            .id("primary_panel_holder")
                    )
                    .child(
                        verticalFlow(Sizing.content(), Sizing.content())
                            .allowOverflow(true)
                            .zIndex(200)
                            .id("right_panel_holder")
                    ).surface((context, component) -> {
                        var primaryLayout = component.childById(FlowLayout.class, "primary_panel_holder");
                        var rightLayout = component.childById(FlowLayout.class, "right_panel_holder").childById(FlowLayout.class, "settings_panel");

                        if (rightLayout == null){
                            Surface.PANEL.draw(context, primaryLayout);

                            return;
                        }

                        ScissorStack.push(rightLayout.x() + 3, rightLayout.y(), rightLayout.width(), rightLayout.height(), context.getMatrices());
                        Surface.PANEL.draw(context, rightLayout);
                        ScissorStack.pop();

                        context.draw();
                        ScissorStack.push(primaryLayout.x(), primaryLayout.y(), primaryLayout.width() - 3, primaryLayout.height(), context.getMatrices());
                        context.draw();
                        Surface.PANEL.draw(context, primaryLayout);
                        ScissorStack.pop();

                        ScissorStack.push(primaryLayout.x(), primaryLayout.y(), primaryLayout.width(), rightLayout.y() - primaryLayout.y() + 1, context.getMatrices());
                        context.draw();
                        Surface.PANEL.draw(context, primaryLayout);
                        ScissorStack.pop();

                        ScissorStack.push(primaryLayout.x(), rightLayout.y() + rightLayout.height() - 1, primaryLayout.width(), primaryLayout.y() + primaryLayout.height() - (rightLayout.y() + rightLayout.height()), context.getMatrices());
                        context.draw();
                        Surface.PANEL.draw(context, primaryLayout);
                        ScissorStack.pop();
                    })
            )
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);
    }

    private void setupSettingsWindow(UUID uuid) {
        var rightPanelHolder = component(FlowLayout.class, "right_panel_holder");

        rightPanelHolder.clearChildren();

        if(!openSettingsUUID.equals(uuid) && !uuid.equals(WaystoneData.EMPTY_UUID)) {
            rightPanelHolder.child(createSettingsComponent(uuid));
        } else {
            uuid = WaystoneData.EMPTY_UUID;
        }

        openSettingsUUID = uuid;

        var leftPanelHolder = component(FlowLayout.class, "left_panel_holder");

        leftPanelHolder.padding(Insets.left(getLeftPadding()));
    }

    private UUID openSettingsUUID = WaystoneData.EMPTY_UUID;

    private Component createSettingsComponent(UUID uuid) {
        var data = storage.getData(uuid);

        return verticalFlow(Sizing.content(), Sizing.content())
            .child(
                verticalFlow(Sizing.fixed(120), Sizing.fixed(120))
                    .child(
                        verticalScroll(Sizing.expand(), Sizing.expand(),
                            verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                    horizontalFlow(Sizing.content(), Sizing.content())
                                        .child(label(Text.of("ID:")))
                                        .child(label(Text.of(uuid.toString())))
                                )
                                .<FlowLayout>configure(layout -> {
                                    if (data instanceof NetworkedWaystoneData networkedWaystoneData) {
                                        layout.child(
                                            horizontalFlow(Sizing.content(), Sizing.content())
                                                .child(label(Text.of("Name:")))
                                                .child(label(networkedWaystoneData.parsedName()))
                                        );
                                    }
                                })
                                .child()
                                .gap(3)
                            )
                            .surface(Surface.PANEL_INSET)
                            .padding(Insets.of(3))
                    )
                    .padding(Insets.of(7, 7, 5, 7))
                    .id("settings_panel")
            )
            .padding(Insets.of(24, 0, -8, 0));
    }

    private int getLeftPadding() {
        if (openSettingsUUID != WaystoneData.EMPTY_UUID) {
            return 120 - 8;
        }

        return 0;
    }

    private void setWaystoneList() {
        var textBox = this.component(TextBoxComponent.class, "search_box");

        setWaystoneList(textBox != null ? textBox.getText() : "");
    }

    private void setWaystoneList(String searchText) {
        waystoneList.child(createWaystoneList(searchText));
    }

    private FlowLayout createWaystoneList(String searchText) {
        return list(
            getSortedWaystones(searchText),
            flowLayout -> {},
            this::createButtonLayout,
            true
        );
    }

    private boolean selectingUUIDs = false;
    private final Set<UUID> selectedUUIDs = new HashSet<>();

    private Component createButtonLayout(UUID uuid) {
        // TODO: BETTER HANDLING FOR THIS
        if (!(storage.getData(uuid) instanceof NetworkedWaystoneData networkedData)) {
            throw new IllegalStateException("This should not happen and idk how it did");
        }
        var data = storage.getData(uuid);
        var existsWithinTheWorld = storage.getPosition(uuid) != null;

        // TODO: MAYBE CONVERT TO DROP DOWN?
        var buttonLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .<FlowLayout>configure(component -> {
                    component
                        .gap(2)
                        .positioning(Positioning.relative(100, 50))
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .padding(Insets.of(2))
                        .id("button_layout");
                });

        if (!data.isDefaultColor()) {
            buttonLayout
                .child(
                    wrapNonInteractive(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                            .child(
                                box(Sizing.fixed(8), Sizing.fixed(8))
                                    .fill(true)
                                    .color(Color.ofRgb(data.color()))
                            )
                            .padding(Insets.of(1))
                            .surface(Surface.outline(Color.BLACK.interpolate(Color.WHITE, 0.2f).argb()))
                    )
                );
        }

        buttonLayout.child(
            Components.button(Text.empty(), btn -> setupSettingsWindow(uuid))
                .renderer(ButtonComponent.Renderer.texture(FabricWaystones.id("textures/gui/settings_icon.png"), 0, 0, 12, 12))
                .sizing(Sizing.fixed(12))
        );

        Supplier<Component> favoriteIconBuilder = () -> {
            return texture(FAVORITE_ICON, 0, 10, 10, 10, 10, 20)
                .id("favorite_icon");
        };

        if (playerData.isFavorited(uuid)) {
            buttonLayout.child(0, favoriteIconBuilder.get());
        }

        return new FlowLayout(Sizing.expand(), Sizing.fixed(16), FlowLayout.Algorithm.HORIZONTAL) {
            @Override
            public boolean canFocus(FocusSource source) {
                return true;
            }

            @Override
            protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
                super.updateHoveredState(mouseX, mouseY, nowHovered);

                if (nowHovered && !this.hovered) {
                    this.hovered = true;
                    this.mouseEnterEvents.sink().onMouseEnter();
                }
            }
        }.configure((FlowLayout waystoneButton) -> {
            final var buttonState = new Interactable.Mutable(existsWithinTheWorld) {
                @Override
                public InteractionType interact() {
                    var cords = getMouseCords();

                    var hoveringTarget = waystoneButton.childAt(cords.x(), cords.y());

                    var interactionType = super.interact();

                    if (selectingUUIDs) {
                        if (selectedUUIDs.contains(uuid)) {
                            interactionType = InteractionType.HOVERED;
                        } else {
                            interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                        }

                        return interactionType;
                    }

                    if (hoveringTarget != null) {
                        if (waystoneButton.children().contains(hoveringTarget)) {
                            interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                        } else if(hoveringTarget.parent() instanceof ParentComponent parentHover && waystoneButton.children().contains(parentHover)) {
                            interactionType = (interactionType != InteractionType.HOVERED ? interactionType : InteractionType.ENABLED);
                        }
                    }

                    return interactionType;
                }
            };

            if (existsWithinTheWorld) {
                waystoneButton.mouseEnter().subscribe(() -> {
                    buttonState.interact(true);
                });

                waystoneButton.mouseLeave().subscribe(() -> {
                    buttonState.interact(false);
                });

                waystoneButton.mouseUp().subscribe((mouseX, mouseY, button) -> {
                    if (!buttonState.interact().equals(Interactable.InteractionType.HOVERED)) return false;

                    if (button == 0) {
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                        this.handler.attemptTeleport(uuid);

                        return true;
                    } else if (button == 1) {
                        var cords = getMouseCords();

                        ExperimentalWaystoneScreen.this.uiAdapter.rootComponent.child(
                            new BetterDropdownComponent(Sizing.fixed(100))
                                .onDismount(reason -> {
                                    selectingUUIDs = false;
                                    selectedUUIDs.clear();
                                })
                                .layout(layout -> {
                                    layout
                                        .child(texture(FAVORITE_ICON, 0, 10, 10, 10, 10, 20).margins(Insets.right(1)))
                                        .child(label(playerData.isFavorited(uuid) ? Text.of("Unfavorite") : Text.of("Favorite")).id("favorite_action_label"))
                                        .gap(2)
                                        .verticalAlignment(VerticalAlignment.CENTER)
                                        .verticalSizing(Sizing.fixed(13));
                                }, comp -> {
                                    if (playerData.isFavorited(uuid)) {
                                        playerData.removeFavoriteWaystone(uuid);

                                        removeIfPresent(buttonLayout, "favorite_icon");
                                    } else {
                                        playerData.addFavoriteWaystone(uuid);

                                        addIfMissing(buttonLayout, "favorite_icon", favoriteIconBuilder);
                                    }

                                    var label = comp.childById(LabelComponent.class, "favorite_action_label");

                                    label.text(playerData.isFavorited(uuid) ? Text.of("Unfavorite") : Text.of("Favorite"));

                                    setWaystoneList();
                                })
                                .layout(layout -> {
                                    layout
                                        .child(texture(FabricWaystones.id("textures/gui/garbage_buttons.png"), 0, 0, 11, 13, 11, 39))
                                        .child(label(Text.of("Forget")).margins(Insets.top(1)))
                                        .gap(2)
                                        .verticalAlignment(VerticalAlignment.CENTER);
                                }, comp -> {
                                    // TODO: ADD CONFIRM OVERLAY OF ACTION TO FORGET
                                    playerData.forgetWaystone(uuid);

                                    setWaystoneList();

                                    comp.remove();
                                })
                                .closeWhenNotHovered(true)
                                .positioning(Positioning.absolute(cords.x() - 4, cords.y() - 4))
                                .zIndex(500)
                                .id("configure_waystone_dropdown")
                        );

                        selectingUUIDs = true;
                        selectedUUIDs.add(uuid);

                        return true;
                    }

                    return false;
                });
            }

            waystoneButton.surface(
                RenderBuilder.surface()
                    .ninePatched(ACTIVE_TEXTURE, HOVERED_TEXTURE, DISABLED_TEXTURE, buttonState)
            );
        })
            .child(
                wrapNonInteractive(
                    label(networkedData.parsedName())
                        .margins(Insets.left(2))
                )
            )
            .child(buttonLayout)
            .gap(2)
            .padding(Insets.of(2))
            .verticalAlignment(VerticalAlignment.CENTER);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) == 0
            && this.uiAdapter.rootComponent.focusHandler().focused() instanceof GreedyInputComponent inputComponent
            && keyCode != GLFW.GLFW_KEY_ESCAPE) {

            inputComponent.onKeyPress(keyCode, scanCode, modifiers);

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
