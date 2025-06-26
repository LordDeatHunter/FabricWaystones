package wraith.fwaystones.client.screen;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.NinePatchTexture;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.client.screen.components.ComponentUtils;

import static io.wispforest.owo.ui.container.Containers.*;
import static io.wispforest.owo.ui.component.Components.*;

import java.util.*;

public class ExperimentalWaystoneScreen extends BaseOwoHandledScreen<FlowLayout, ExperimentalWaystoneScreenHandler> {

    public static final Identifier ACTIVE_TEXTURE = FabricWaystones.id("waystone_button/active");
    public static final Identifier HOVERED_TEXTURE = FabricWaystones.id("waystone_button/hovered");
    public static final Identifier DISABLED_TEXTURE = FabricWaystones.id("waystone_button/disabled");

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
        var existingWaystones = playerData.discoveredWaystones().stream()
            .map(storage::getData)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(WaystoneData::sortingName))
            .toList();

        return (searchText.isBlank()
            ? existingWaystones.stream().map(WaystoneData::uuid)
            : FuzzySearch.extractSorted(searchText.toLowerCase(Locale.ROOT), existingWaystones, WaystoneData::sortingName, 75).stream().map(result -> result.getReferent().uuid()))
//            .sorted(Comparator.comparing(data -> storage.getPosition(data) == null))
            .toList();

//        return
//            .sorted((uuid1, uuid2) -> {
//                var firstExists = storage.getPosition(uuid1) != null;
//                var secondExits = storage.getPosition(uuid2) != null;
//
//                if (firstExists && secondExits) return 0;
//                if (firstExists) return 1;
//                return -1;
//            })
//            .subList();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        // TODO: ADD EVENT HOOKS FOR EITHER DATA CHANGES OR DISCOVERY OR WAYSTONE REPOSITINONED
        waystoneList = verticalScroll(
            Sizing.fixed(120), Sizing.fixed(120),
            createWaystoneList("")
        );

        rootComponent.child(
                verticalFlow(Sizing.content(), Sizing.content())
                    .child(
                        Components.textBox(Sizing.fixed(120), "")
                            .<TextBoxComponent>configure(textBox -> textBox.onChanged().subscribe(newText -> waystoneList.child(createWaystoneList(newText))))
                    )
                    .child(waystoneList
                               .surface(Surface.panelWithInset(0))
                               .padding(Insets.of(1))
                               .allowOverflow(false))
                    .allowOverflow(true)
                    .surface(Surface.PANEL)
                    .padding(Insets.of(7)))
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER);
    }

    private FlowLayout createWaystoneList(String searchText) {
        return Components.list(
            getSortedWaystones(searchText),
            flowLayout -> {},
            this::createButtonLayout,
            true
        );
    }

    private FlowLayout createButtonLayout(UUID uuid) {
        var data = storage.getData(uuid);
        var existsWithinTheWorld = storage.getPosition(uuid) != null;

        return new FlowLayout(Sizing.expand(), Sizing.content(), FlowLayout.Algorithm.VERTICAL) {
            @Override
            public boolean canFocus(FocusSource source) {
                return true;
            }
        }.configure((FlowLayout waystoneButton) -> {
                final var buttonState = new MutableInt(existsWithinTheWorld ? 0 : -1);

                if (existsWithinTheWorld) {
                    waystoneButton.mouseEnter().subscribe(() -> {
                        var state = buttonState.getValue();

                        if (state != -1) buttonState.setValue(1);
                    });

                    waystoneButton.mouseLeave().subscribe(() -> {
                        var state = buttonState.getValue();

                        if (state != -1) buttonState.setValue(0);
                    });
                }

                waystoneButton.surface((context, component) -> {
                    var texture = switch (buttonState.getValue()) {
                        case -1 -> DISABLED_TEXTURE;
                        case 1 -> HOVERED_TEXTURE;
                        default -> ACTIVE_TEXTURE;
                    };
                    var color = Color.ofRgb(data.color());
                    context.setShaderColor(color.red(), color.green(), color.blue(), 1.0F);
                    NinePatchTexture.draw(texture, context, component.x(), component.y(), component.width(), component.height());
                    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                });

                if (existsWithinTheWorld) {
                    waystoneButton.mouseUp().subscribe((mouseX, mouseY, button) -> {
                        if (button != 0) return false;
                        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                        this.handler.attemptTeleport(uuid);

                        return true;
                    });
                }

                waystoneButton.padding(Insets.of(3));
            })
            .child(ComponentUtils.wrapNonInteractive(Components.label(data.parsedName())));
    }
}
