package wraith.fwaystones.client.screen;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import io.wispforest.owo.ui.util.ScissorStack;
import io.wispforest.owo.ui.util.UIErrorToast;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.wispforest.owo.ui.container.Containers.*;
import static wraith.fwaystones.client.screen.components.ComponentUtils.*;
import static io.wispforest.owo.ui.component.Components.*;

public class ExperimentalWaystoneScreen extends BaseOwoHandledScreen<FlowLayout, ExperimentalWaystoneScreenHandler> {

    public static final Identifier FAVORITE_ICON = FabricWaystones.gui("favorite_icon");
    public static final Identifier GLOBAL_ICON = FabricWaystones.gui("global_icon");

    private static final String leftPanelHolderId = "left_panel_holder";
    private static final String leftPanelContentsId = "left_panel_contents";

    private static final String rightPanelHolderId = "right_panel_holder";

    //--

    public final WaystoneDataStorage storage;
    public final WaystonePlayerData playerData;

    private final int columnWidth = 140;

    private final int rightPanelWidth = 140;
    private final int leftPanelWidth = 140;

    //--

    private final ScrollContainer<FlowLayout> waystoneList = verticalScroll(
        Sizing.fixed(columnWidth), Sizing.fixed(columnWidth + 20),
        verticalFlow(Sizing.content(), Sizing.content())
    );

    public ExperimentalWaystoneScreen(ExperimentalWaystoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.playerInventoryTitleX = 42069;
        this.titleX = 42069;

        this.storage = WaystoneDataStorage.getStorage(inventory.player);
        this.playerData = WaystonePlayerData.getData(inventory.player);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        setWaystoneComponents("");
        setWaystoneList();

        rootComponent.child(
                horizontalFlow(Sizing.content(), Sizing.content())
                    .child(
                        stack(Sizing.content(), Sizing.content())
                            .child(
                                button(Text.of(" < "), btn -> {
                                    toggleFilteredPanel();
                                }).horizontalSizing(Sizing.fixed(16))
                                    .verticalSizing(Sizing.fixed(12))
                                    .tooltip(Text.of("Toggle Filters"))
                                    .margins(Insets.top(11))
                            )
                            .child(
                                verticalFlow(Sizing.content(), Sizing.content())
                                    .allowOverflow(true)
                                    .zIndex(200)
                                    .id(leftPanelContentsId)
                            )
                            .allowOverflow(true)
                            .horizontalAlignment(HorizontalAlignment.RIGHT)
                            .id(leftPanelHolderId)
                    )
                    .child(
                        verticalFlow(Sizing.content(), Sizing.content())
                            .child(
                                textBox(Sizing.fixed(columnWidth), "")
                                    .<TextBoxComponent>configure(textBox -> textBox.onChanged()
                                        .subscribe(value -> {
                                            setWaystonesView(value);
                                            setWaystoneList();
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
                            .id(rightPanelHolderId)
                    ).surface(CURSED_SURFACE)
            )
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);

        setupPadding();
    }

    //--

    private final Map<UUID, FlowLayout> waystoneEntries = new HashMap<>();

    private List<UUID> waystones = List.of();
    private boolean filteredList = false;

    private void updateWaystoneView() {
        setWaystonesView(this.component(TextBoxComponent.class, "search_box").getText());
        setWaystoneList();
    }

    private void setWaystonesView(String searchText) {
        var sortedWaystones = playerData.discoveredWaystones().stream()
            .map(uuid -> storage.getDataIfType(uuid, NetworkedWaystoneData.class))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(NetworkedWaystoneData::sortingName))
            .toList();

        filteredList = !searchText.isBlank();

        if (filteredList) {
            waystones = FuzzySearch.extractSorted(searchText.toLowerCase(Locale.ROOT), sortedWaystones, NetworkedWaystoneData::sortingName, 75)
                .stream()
                .map(result -> result.getReferent().uuid())
                .toList();
        } else {
            waystones = sortWaystones(sortedWaystones.stream().map(WaystoneData::uuid));
        }
    }

    private List<UUID> sortWaystones(Stream<UUID> waystones) {
        return waystones
            .sorted(Comparator.comparing(storage::hasPosition).reversed())
            .sorted(Comparator.comparing(playerData::isFavorited).reversed())
            .toList();
    }

    private void setWaystoneList() {
        waystoneList.child(list(waystones, flowLayout -> {}, this.waystoneEntries::get, true));
    }

    private void setWaystoneComponents(String searchText) {
        var prevWaystones = waystones;

        setWaystonesView(searchText);

        if (filteredList || waystones.equals(prevWaystones)) return;

        for (var waystone : this.waystones) {
            this.waystoneEntries.putIfAbsent(waystone, createButtonLayoutAndHolder(waystone));
        }
    }

    //--

    private int getLeftPadding() {
        var leftWidth = getLeftPanelWidth();
        var rightWidth = getRightPanelWidth();

        if (leftWidth >= rightWidth) return 0;

        return rightWidth - leftWidth;
    }

    private int getRightPadding() {
        var leftWidth = getLeftPanelWidth();
        var rightWidth = getRightPanelWidth();

        if (rightWidth >= leftWidth) return 0;

        return leftWidth - rightWidth;
    }

    private int getRightPanelWidth() {
        return (openSettingsUUID != WaystoneData.EMPTY_UUID) ? rightPanelWidth - 8 : 0;
    }

    private int getLeftPanelWidth() {
        return isFilteredPanelsOpen ? leftPanelWidth - 8 : 16;
    }

    private void setupPadding() {
        var rightPanelHolder = component(FlowLayout.class, rightPanelHolderId);

        rightPanelHolder.padding(Insets.right(getRightPadding()));

        var leftPanelHolder = component(ParentComponent.class, leftPanelHolderId);

        leftPanelHolder.padding(Insets.left(getLeftPadding()));
    }

    private Component createPanelLayout(boolean isOnRight, int width, String id, Consumer<FlowLayout> addCallback) {
        return verticalFlow(Sizing.content(), Sizing.content())
            .child(
                verticalFlow(Sizing.fixed(width), Sizing.fixed(140))
                    .configure(addCallback)
                    .padding(Insets.of(7, 7, isOnRight ? 5 : 7, isOnRight ? 7 : 5))
                    .id(id)
            )
            .padding(Insets.of(24, 0, isOnRight ? -8 : 0, isOnRight ? 0 : -8));
    }

    //--

    private void closeSettingsWindowIfOpen(UUID uuid) {
        if (!openSettingsUUID.equals(uuid)) return;

        setupSettingsWindow(WaystoneData.EMPTY_UUID);
    }

    private void resetSettingsWindowIfOpen(UUID uuid) {
        if (!openSettingsUUID.equals(uuid)) return;

        openSettingsUUID = WaystoneData.EMPTY_UUID;

        setupSettingsWindow(uuid);
    }

    public void setupSettingsWindow(UUID uuid) {
        var rightPanelHolder = component(FlowLayout.class, rightPanelHolderId);

        rightPanelHolder.clearChildren();

        if(!openSettingsUUID.equals(uuid) && !uuid.equals(WaystoneData.EMPTY_UUID)) {
            rightPanelHolder.child(createSettingsComponent(uuid));
        } else {
            uuid = WaystoneData.EMPTY_UUID;
        }

        openSettingsUUID = uuid;

        setupPadding();
    }

    private UUID openSettingsUUID = WaystoneData.EMPTY_UUID;

    private Component createSettingsComponent(UUID uuid) {
        var data = storage.getData(uuid);

        return createPanelLayout(true, rightPanelWidth, "settings_panel", panelLayout -> {
            panelLayout.child(
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

                                        updateWaystoneView();
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
            );
        });
    }

    //--

    private void toggleFilteredPanel() {
        var leftPanelContents = component(FlowLayout.class, leftPanelContentsId);

        if (isFilteredPanelsOpen) {
            leftPanelContents.clearChildren();
        } else {
            leftPanelContents.child(createFiltersComponent());
        }

        isFilteredPanelsOpen = !isFilteredPanelsOpen;

        setupPadding();
    }

    private boolean isFilteredPanelsOpen = false;

    /*
     * Filter and sort options:
     *
     * Show / Hide Global Waystones
     * Show / Hide Favorites Waystones
     * Show / Hide Discovered Waystones
     * Search by: Fuzzy | Contains | Starts With
     *
     * Other Options:
     * Auto Focus Search Box
     * Auto Open Settings for current Waystone
     * Show Debug Info
     */
    private Component createFiltersComponent() {
        return createPanelLayout(false, leftPanelWidth, "filter_panel", panelLayout -> {
            panelLayout.child(
                verticalFlow(Sizing.expand(), Sizing.expand())
                    .surface(Surface.PANEL_INSET)
                    .padding(Insets.of(3))
            );
        });
    }

    //--

    public boolean selectingUUIDs = false;
    public final Set<UUID> selectedUUIDs = new HashSet<>();

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

    private ParentComponent createButtonLayout(UUID uuid) {
        return (ParentComponent) new WaystoneButtonLayout(this, Sizing.expand(), Sizing.fixed(16), uuid)
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

    public void addOverlay(UUID uuid) {
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

                    updateWaystoneView();
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

                    var data = storage.getDataIfTypeOrThrow(uuid, NetworkedWaystoneData.class);

                    var confirmPanel = (FlowLayout) verticalFlow(Sizing.content(), Sizing.content())
                        .gap(6)
                        .padding(Insets.of(6))
                        .surface(Surface.flat(0xC7000000).and(Surface.blur(3, 5)).and(Surface.outline(0xFF121212)))
                        .horizontalAlignment(HorizontalAlignment.CENTER);

                    var confirmOverlay = overlay(confirmPanel)
                        .surface(Surface.BLANK)
                        .zIndex(600);

                    comp.remove();

                    // TODO: MAKE "Are you sure?" meme for april fools cause chyz is gigabrained
                    confirmPanel
                        .child(Components.label(Text.of("Do you want to forget: ")))
                        .child(Components.label(data.parsedName()))
                        .child(
                            horizontalFlow(Sizing.content(), Sizing.content())
                                .child(
                                    button(Text.of("Yes"), btn -> {
                                        playerData.forgetWaystone(uuid);

                                        setWaystoneList();

                                        confirmOverlay.remove();
                                    }).verticalSizing(Sizing.fixed(16))
                                        .horizontalSizing(Sizing.fixed(50))
                                ).child(
                                    button(Text.of("No"), btn -> {
                                        confirmOverlay.remove();
                                    }).verticalSizing(Sizing.fixed(16))
                                        .horizontalSizing(Sizing.fixed(50))
                                )
                                .gap(4)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                        );

                    uiAdapter.rootComponent.child(confirmOverlay);
                })
                .closeWhenNotHovered(true)
                .positioning(Positioning.absolute(cords.x() - 4, cords.y() - 4))
                .zIndex(500)
                .id("configure_waystone_dropdown")
        );
    }

    //--

    @Override
    public void render(DrawContext vanillaContext, int mouseX, int mouseY, float delta) {
        try {
            super.render(vanillaContext, mouseX, mouseY, delta);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not initialize owo screen", error);
            UIErrorToast.report(error);
            this.invalid = true;

            this.close();
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

    //--

    // Hooks for updating screen based on variours data changes
    static {
        WaystoneEvents.ON_WAYSTONE_DISCOVERY.register((player, uuid, position) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                screen.waystoneEntries.putIfAbsent(uuid, screen.createButtonLayoutAndHolder(uuid));

                screen.updateWaystoneView();
            }
        });

        WaystoneEvents.ON_WAYSTONE_FORGOTTEN.register((player, uuid, position) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                screen.waystoneEntries.remove(uuid);

                screen.updateWaystoneView();

                screen.closeSettingsWindowIfOpen(uuid);
            }
        });

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ExperimentalWaystoneScreen screen) {
                if (type.equals(DataChangeType.REMOVAL)) {
                    screen.waystoneEntries.remove(uuid);

                    screen.updateWaystoneView();

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

    private static final Surface CURSED_SURFACE = (context, component) -> {
        // TODO: REPLACE CURSED SURFACE WITH BAKED TEXTURE
        var primaryLayout = component.childById(FlowLayout.class, "primary_panel_holder");

        var rightLayout = component.childById(FlowLayout.class, rightPanelHolderId).childById(FlowLayout.class, "settings_panel");
        var leftLayout  = component.childById(StackLayout.class, leftPanelHolderId).childById(FlowLayout.class, "filter_panel");

        Surface.PANEL.draw(context, primaryLayout);

        if (rightLayout == null && leftLayout == null) return;

        if (rightLayout != null){
            ScissorStack.push(rightLayout.x() + 3, rightLayout.y(), rightLayout.width(), rightLayout.height(), context.getMatrices());
            Surface.PANEL.draw(context, rightLayout);
            ScissorStack.pop();

            ScissorStack.push(primaryLayout.x(), primaryLayout.y(), primaryLayout.width(), rightLayout.y() - primaryLayout.y() + 1, context.getMatrices());
            context.draw();
            Surface.PANEL.draw(context, primaryLayout);
            ScissorStack.pop();

            ScissorStack.push(primaryLayout.x(), rightLayout.y() + rightLayout.height() - 1, primaryLayout.width(),
                primaryLayout.y() + primaryLayout.height() - (rightLayout.y() + rightLayout.height()), context.getMatrices());
            context.draw();
            Surface.PANEL.draw(context, primaryLayout);
            ScissorStack.pop();
        }

        if (leftLayout != null){
            ScissorStack.push(leftLayout.x(), leftLayout.y(), leftLayout.width() - 3, leftLayout.height(), context.getMatrices());
            Surface.PANEL.draw(context, leftLayout);
            ScissorStack.pop();

            ScissorStack.push(primaryLayout.x(), primaryLayout.y(), primaryLayout.width(), leftLayout.y() - primaryLayout.y() + 1, context.getMatrices());
            context.draw();
            Surface.PANEL.draw(context, primaryLayout);
            ScissorStack.pop();

            ScissorStack.push(primaryLayout.x(), leftLayout.y() + leftLayout.height() - 1, primaryLayout.width(),
                primaryLayout.y() + primaryLayout.height() - (leftLayout.y() + leftLayout.height()), context.getMatrices());
            context.draw();
            Surface.PANEL.draw(context, primaryLayout);
            ScissorStack.pop();
        }

        context.draw();
        ScissorStack.push(primaryLayout.x() + 3, primaryLayout.y(), primaryLayout.width() - 6, primaryLayout.height(), context.getMatrices());
        context.draw();
        Surface.PANEL.draw(context, primaryLayout);
        ScissorStack.pop();
    };
}
