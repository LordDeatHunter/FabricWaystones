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
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.client.screen.components.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.wispforest.owo.ui.container.Containers.*;
import static wraith.fwaystones.client.screen.components.ComponentUtils.*;
import static io.wispforest.owo.ui.component.Components.*;

public class ExperimentalWaystoneScreen extends BaseOwoHandledScreen<FlowLayout, ExperimentalWaystoneScreenHandler> {

    public static final Identifier FAVORITE_ICON = FabricWaystones.gui("favorite_icon");
    public static final Identifier GLOBAL_ICON = FabricWaystones.gui("global_icon");

    private final WaystoneDataStorage storage;
    private final WaystonePlayerData playerData;

    private final int columnWidth = 140;
    private final int rightPanelWidth = 140;

    private final ScrollContainer<FlowLayout> waystoneList = verticalScroll(
        Sizing.fixed(columnWidth), Sizing.fixed(columnWidth + 20),
        verticalFlow(Sizing.content(), Sizing.content())
    );

    public ExperimentalWaystoneScreen(ExperimentalWaystoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.playerInventoryTitleX = 42069;
        this.storage = WaystoneDataStorage.getStorage(inventory.player);
        this.playerData = WaystonePlayerData.getData(inventory.player);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    private final Map<UUID, FlowLayout> waystoneEntries = new HashMap<>();

    private List<UUID> waystones = List.of();
    private boolean filteredList = false;

    private void setWaystonesView(String searchText) {
        var sortedWaystones = playerData.discoveredWaystones().stream()
            .map(uuid -> storage.getDataIfType(uuid, NetworkedWaystoneData.class))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(NetworkedWaystoneData::sortingName))
            .toList();

        if (searchText.isBlank()) {
            waystones = sortedWaystones.stream()
                .map(WaystoneData::uuid)
                .sorted(Comparator.comparing(storage::hasPosition).reversed())
                .sorted(Comparator.comparing(playerData::isFavorited).reversed())
                .toList();

            filteredList = false;
        } else {
            waystones = FuzzySearch.extractSorted(searchText.toLowerCase(Locale.ROOT), sortedWaystones, NetworkedWaystoneData::sortingName, 75)
                .stream()
                .map(result -> result.getReferent().uuid())
                .toList();

            filteredList = true;
        }
    }

    private void setWaystoneComponents(String searchText) {
        var prevWaystones = waystones;

        setWaystonesView(searchText);

        if (!filteredList && !waystones.equals(prevWaystones)) {
            for (var waystone : this.waystones) {
                this.waystoneEntries.putIfAbsent(waystone, createButtonLayoutAndHolder(waystone));
            }
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        setWaystoneComponents("");
        setWaystoneList();

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
                                        .subscribe(value -> {
                                            this.setWaystonesView(value);
                                            this.setWaystoneList();
                                        }))
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
                        // TODO: REPLACE CURSED SURFACE WITH BAKED TEXTURE
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

    private void closeSettingsWindowIfOpen(UUID uuid) {
        if (!openSettingsUUID.equals(uuid)) return;

        closeSettingsWindow();
    }

    private void resetSettingsWindowIfOpen(UUID uuid) {
        if (!openSettingsUUID.equals(uuid)) return;

        openSettingsUUID = WaystoneData.EMPTY_UUID;

        setupSettingsWindow(uuid);
    }

    private void closeSettingsWindow() {
        setupSettingsWindow(WaystoneData.EMPTY_UUID);
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
                verticalFlow(Sizing.fixed(rightPanelWidth), Sizing.fixed(140))
                    .child(
                        verticalScroll(Sizing.expand(), Sizing.expand(),
                            verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                    horizontalFlow(Sizing.content(), Sizing.content())
                                        .child(label(Text.of("ID:")))
                                        .child(label(Text.of(uuid.toString())))
                                        .gap(3)
                                )
                                .<FlowLayout>configure(layout -> {
                                    if (data instanceof NetworkedWaystoneData networkedWaystoneData) {
                                        var textBox = textBox(Sizing.expand(), networkedWaystoneData.name());

                                        layout.child(
                                            horizontalFlow(Sizing.content(), Sizing.content())
                                                .child(label(Text.of("Name:")))
                                                .child(label(networkedWaystoneData.parsedName()))
                                                .gap(3)
                                        ).child(
                                            horizontalFlow(Sizing.content(), Sizing.content())
                                                .child(textBox.verticalSizing(Sizing.fixed(17)).margins(Insets.of(-1)))
                                                .child(button(Text.of("[]"), btn -> {
                                                    storage.renameWaystone(uuid, textBox.getText());
                                                }).verticalSizing(Sizing.fixed(17)))
                                                .gap(3)
                                        );
                                    }
                                })
                                .child(
                                    horizontalFlow(Sizing.content(), Sizing.content())
                                        .child(label(Text.of("Visibility:")))
                                        .child(
                                            button(storage.isGlobal(uuid) ? Text.of("Global") : Text.of("Local"), btn -> {
                                                var state = storage.isGlobal(uuid);

                                                btn.setMessage(!state ? Text.of("Global") : Text.of("Local"));

                                                storage.toggleGlobal(uuid);
                                            }).verticalSizing(Sizing.fixed(17))
                                                .horizontalSizing(Sizing.fixed(40))
                                        ).gap(3)
                                        .verticalAlignment(VerticalAlignment.CENTER)
                                )
                                .child(
                                    horizontalFlow(Sizing.content(), Sizing.content())
                                        .child(label(Text.of("Favorited:")))
                                        .child(
                                            button(playerData.isFavorited(uuid) ? Text.of("Yes") : Text.of("No"), btn -> {
                                                var state = playerData.toggleFavorite(uuid);

                                                btn.setMessage(state ? Text.of("Yes") : Text.of("No"));

                                                getWaystonesIconLayout(uuid).attemptToUpdateComponents();
                                            }).verticalSizing(Sizing.fixed(17))
                                                .horizontalSizing(Sizing.fixed(30))
                                        ).gap(3)
                                        .verticalAlignment(VerticalAlignment.CENTER)
                                )
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
            return rightPanelWidth - 8;
        }

        return 0;
    }

    private void setWaystoneList() {
        waystoneList.child(list(waystones, flowLayout -> {}, this.waystoneEntries::get, true));
    }

    private boolean selectingUUIDs = false;
    private final Set<UUID> selectedUUIDs = new HashSet<>();

    private FlowLayout createButtonLayoutAndHolder(UUID uuid) {
        return (FlowLayout) verticalFlow(Sizing.content(), Sizing.content())
            .child(createButtonLayout(uuid))
            .id("button_holder");
    }

    private void updateButtonLayout(UUID uuid) {
        var holder = this.waystoneEntries.get(uuid);

        holder.clearChildren();

        holder.child(createButtonLayout(uuid));
    }

    private Component colorIconBuilder(UUID uuid) {
        var data = storage.getData(uuid);

        return wrapNonInteractive(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                    box(Sizing.fixed(8), Sizing.fixed(8))
                        .fill(true)
                        .color(Color.ofRgb(data.color()))
                )
                .padding(Insets.of(1))
                .surface(Surface.outline(Color.BLACK.interpolate(Color.WHITE, 0.2f).argb()))
        );
    }

    private static AdjustableTextureComponent createIcon(Identifier texture){
        return createIcon(texture, true);
    }

    private static AdjustableTextureComponent createIcon(Identifier texture, boolean active){
        return AdjustableTextureComponent.of(texture, 10, 10, 10, 20)
            .setV(active ? 10 : 0);
    }

    private ParentComponent createButtonLayout(UUID uuid) {
        return (ParentComponent) new ButtonLayout(Sizing.expand(), Sizing.fixed(16), uuid)
            .gap(2)
            .padding(Insets.of(2))
            .verticalAlignment(VerticalAlignment.CENTER)
            .id(uuid.toString());
    }

    private ParentComponent getWaystoneComponent(UUID uuid) {
        return this.waystoneEntries.get(uuid);
    }

    private DefinedOrderParentComponent getWaystonesIconLayout(UUID uuid){
        return getWaystoneComponent(uuid)
            .childById(DefinedOrderParentComponent.class, "button_layout");
    }

    private void addOverlay(UUID uuid) {
        var cords = getMouseCords();

        Supplier<DefinedOrderParentComponent> iconLayoutAccess = () -> getWaystonesIconLayout(uuid);

        this.uiAdapter.rootComponent.child(
            new BetterDropdownComponent(Sizing.fixed(100))
                .onDismount(reason -> {
                    selectingUUIDs = false;
                    selectedUUIDs.clear();
                })
                .layout(layout -> {
                    layout
                        .child(createIcon(FAVORITE_ICON, !playerData.isFavorited(uuid)).margins(Insets.right(1)).id("favorite_icon"))
                        .child(label(playerData.isFavorited(uuid) ? Text.of("Unfavorite") : Text.of("Favorite")).id("favorite_action_label"))
                        .gap(2)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .verticalSizing(Sizing.fixed(13));
                }, comp -> {
                    var state = playerData.toggleFavorite(uuid);

                    iconLayoutAccess.get().attemptToUpdateComponents();

                    comp.childById(LabelComponent.class, "favorite_action_label")
                        .text(state ? Text.of("Unfavorite") : Text.of("Favorite"));

                    comp.childById(AdjustableTextureComponent.class, "favorite_icon")
                        .setV(state ? 0 : 10);

                    setWaystoneList();
                })
                .layout(layout -> {
                    layout
                        .child(createIcon(GLOBAL_ICON, !storage.isGlobal(uuid)).margins(Insets.right(1)).id("global_icon"))
                        .child(label(storage.isGlobal(uuid) ? Text.of("Set Local") : Text.of("Set Global")).id("global_action_label"))
                        .gap(2)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .verticalSizing(Sizing.fixed(13));
                }, comp -> {
                    var state = !storage.isGlobal(uuid);

                    iconLayoutAccess.get().updateComponentsWithOverrides(Map.of("global_icon", state));

                    storage.toggleGlobal(uuid);

                    comp.childById(LabelComponent.class, "global_action_label")
                        .text(state ? Text.of("Set Local") : Text.of("Set Global"));

                    comp.childById(AdjustableTextureComponent.class, "global_icon")
                        .setV(state ? 0 : 10);
                })
                .layout(layout -> {
                    layout
                        .child(texture(FabricWaystones.gui("garbage_buttons"), 0, 0, 11, 13, 11, 39))
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
    }

    private class ButtonLayout extends FlowLayout {

        private static final Identifier ACTIVE_TEXTURE = FabricWaystones.id("waystone_button/active");
        private static final Identifier HOVERED_TEXTURE = FabricWaystones.id("waystone_button/hovered");
        private static final Identifier DISABLED_TEXTURE = FabricWaystones.id("waystone_button/disabled");

        private final Interactable buttonState;

        protected ButtonLayout(Sizing horizontalSizing, Sizing verticalSizing, UUID uuid) {
            super(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);

            var data = storage.getDataIfTypeOrThrow(uuid, NetworkedWaystoneData.class);
            var existsWithinWorld = storage.getPosition(uuid) != null;

            this.buttonState = new Interactable.Mutable(existsWithinWorld) {
                @Override
                public InteractionType interact() {
                    var cords = getMouseCords();
                    var layout = ButtonLayout.this;

                    var hoveringTarget = layout.childAt(cords.x(), cords.y());

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

                        ExperimentalWaystoneScreen.this.handler.attemptTeleport(uuid);

                        return true;
                    } else if (button == 1) {
                        selectingUUIDs = true;
                        selectedUUIDs.add(uuid);

                        ExperimentalWaystoneScreen.this.addOverlay(uuid);

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
                            Components.button(Text.empty(), btn -> setupSettingsWindow(uuid))
                                .renderer(ButtonComponent.Renderer.texture(FabricWaystones.gui("settings_icon"), 0, 0, 12, 12))
                                .sizing(Sizing.fixed(12))
                        )
                        .addConditional("color_icon", () -> colorIconBuilder(uuid), () -> !data.isDefaultColor())
                        .addConditional("global_icon", () -> createIcon(GLOBAL_ICON), () -> storage.isGlobal(uuid))
                        .addConditional("favorite_icon", () -> createIcon(FAVORITE_ICON), () -> playerData.isFavorited(uuid))
                        .configureBaseLayout(layout -> {
                            layout.gap(2)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .padding(Insets.of(2));
                        })
                        .positioning(Positioning.relative(100, 50))
                        .id("button_layout")
                );
        }

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

    // Hooks for updating screen based on variours data changes
    static {
        WaystoneEvents.ON_WAYSTONE_DISCOVERY.register((player, uuid, position) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                screen.waystoneEntries.putIfAbsent(uuid, screen.createButtonLayoutAndHolder(uuid));
                screen.setWaystonesView(screen.component(TextBoxComponent.class, "search_box").getText());

                screen.setWaystoneList();
            }
        });

        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.register((player, uuid, position) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                screen.waystoneEntries.remove(uuid);
                screen.setWaystonesView(screen.component(TextBoxComponent.class, "search_box").getText());

                screen.setWaystoneList();

                screen.closeSettingsWindowIfOpen(uuid);
            }
        });

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                if (type.equals(DataChangeType.REMOVAL)) {
                    screen.waystoneEntries.remove(uuid);
                    screen.setWaystonesView(screen.component(TextBoxComponent.class, "search_box").getText());

                    screen.setWaystoneList();

                    screen.closeSettingsWindowIfOpen(uuid);
                } else {
                    screen.updateButtonLayout(uuid);
                    screen.resetSettingsWindowIfOpen(uuid);
                }
            }
        });

        WaystoneEvents.ON_WAYSTONE_POSITION_UPADTE.register((uuid, position, wasRemoved) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                screen.updateButtonLayout(uuid);
                screen.resetSettingsWindowIfOpen(uuid);
            }
        });

        WaystoneEvents.ON_ALL_WAYSTONES_FORGOTTEN.register((player, uuids) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                for (var uuid : uuids) {
                    screen.waystoneEntries.remove(uuid);
                    screen.closeSettingsWindowIfOpen(uuid);
                }

                screen.setWaystoneList();
            }
        });

        WaystoneEvents.ON_PLAYER_WAYSTONE_DATA_UPDATE.register(player -> {
            // TODO: UPDATE SEARCHING OR WHAT EVER?
        });
    }
}
