package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.packets.SyncPlayerFromClientPacket;
import wraith.fwaystones.packets.WaystoneGUISlotClickPacket;
import wraith.fwaystones.util.FWConfigModel;
import wraith.fwaystones.util.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;

public class UniversalWaystoneScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    protected final Inventory inventory;
    protected final ArrayList<Button> buttons = new ArrayList<>();
    protected Identifier texture;
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;
    protected boolean ignoreTypedCharacter;
    protected boolean mousePressed;
    private EditBox searchField;

    public UniversalWaystoneScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 177, 176);
        this.inventory = inventory;
        buttons.add(new Button(140, 25, 13, 13, 225, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((UniversalWaystoneScreenHandler) handler).toggleSearchType();
                searchField.setFocused(((PlayerEntityMixinAccess) minecraft.player).fabricWaystones$autofocusWaystoneFields());
            }

            @Override
            public boolean isVisible() {
                return searchVisible();
            }

            @Override
            public boolean hasToolTip() {
                return true;
            }

            @Override
            public Component tooltip() {
                return ((UniversalWaystoneScreenHandler) handler).getSearchTypeTooltip();
            }
        });

        //Autoselect search lock
        buttons.add(new ToggleableButton(24, 26, 8, 11, 177, 33, 185, 33) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$autofocusWaystoneFields();
                setupTooltip();
            }

            @Override
            public boolean isVisible() {
                return !(UniversalWaystoneScreen.this instanceof WaystoneBlockScreen waystoneBlockScreen) || waystoneBlockScreen.page == WaystoneBlockScreen.Page.WAYSTONES;
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toggleAutofocusWaystoneFields();
                searchField.setFocused(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$autofocusWaystoneFields());
                setupTooltip();
            }

            private void setupTooltip() {
                this.tooltip = this.toggled
                    ? Component.translatable("fwaystones.config.tooltip.unlock_search")
                    : Component.translatable("fwaystones.config.tooltip.lock_search");
            }
        });

        setupButtons();
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientPlayNetworking.send(new SyncPlayerFromClientPacket(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toTagW(new CompoundTag())));
    }

    protected void setupButtons() {
        for (Button button : buttons) {
            button.setup();
        }
    }

    protected boolean searchVisible() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        this.searchField = new EditBox(font, this.leftPos + 37, this.topPos + 27, 93, 10, Component.literal("")) {
            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
                boolean bl = event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width) && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height);
                if (bl && event.button() == 1) {
                    this.setValue("");
                }
                return super.mouseClicked(event, doubled);
            }
        };
        this.searchField.setMaxLength(16);
        this.searchField.setTextColor(0xFFFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setBordered(false);
        this.searchField.setCanLoseFocus(true);
        this.searchField.setValue("");
        this.searchField.setResponder((s) -> {
            this.scrollAmount = 0;
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5D);
            ((UniversalWaystoneScreenHandler) menu).setFilter(this.searchField != null ? this.searchField.getValue() : "");
            ((UniversalWaystoneScreenHandler) menu).filterWaystones();
        });
        this.addWidget(this.searchField);
    }

    @Override
    public void containerTick() {
        if (this.searchField != null && this.searchField.isVisible()) {
//            this.searchField.tick();
            if (((PlayerEntityMixinAccess) minecraft.player).fabricWaystones$autofocusWaystoneFields()) {
                this.searchField.setFocused(true);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        String string = this.searchField.getValue();
        this.init(width, height);
        this.searchField.setValue(string);
        super.resize(width, height);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        int color = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256, color);
        int k = (int) (75.0F * this.scrollAmount);
        context.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 141, topPos + 40 + k, 177 + (this.shouldScroll() ? 0 : 11), 0, 11, 15, 256, 256, color);
        int n = this.scrollOffset + 5;
        // TODO: Merge some of these
        this.renderWaystoneBackground(context, mouseX, mouseY, this.leftPos + 36, this.topPos + 39, n);
        this.renderForgetButtons(context, mouseX, mouseY, this.leftPos + 24, this.topPos + 45);
        renderButtons(context, mouseX, mouseY);
        this.renderCostItem(context, this.leftPos + 23, this.topPos + 136);
        this.renderWaystoneNames(context, this.leftPos + 36, this.topPos + 40, n);
        this.renderWaystoneTooltips(context, mouseX, mouseY, this.leftPos + 36, this.topPos + 39, n);
        this.renderWaystoneAmount(context, this.leftPos + 10, this.topPos + 160);
        this.searchField.extractWidgetRenderState(context, mouseX, mouseY, delta);
        this.renderForgetTooltips(context, mouseX, mouseY, this.leftPos + 24, this.topPos + 45);
        this.renderButtonTooltips(context, mouseX, mouseY);
    }

    protected void renderButtonTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        for (Button button : buttons) {
            if (!button.isVisible() || !button.hasToolTip() || !button.isInBounds(mouseX - this.leftPos, mouseY - this.topPos)) {
                continue;
            }

            context.setTooltipForNextFrame(font, button.tooltip(), mouseX, mouseY);
        }
    }

    private void renderWaystoneAmount(GuiGraphicsExtractor context, int x, int y) {
        context.text(font, Component.translatable("fwaystones.gui.displayed_waystones", this.getDiscoveredCount()), x, y, 0xFF161616, false);
    }

    protected void renderButtons(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        for (Button button : buttons) {
            if (!button.isVisible()) {
                continue;
            }
            int u = button.getU();
            int v = button.getV();
            if (button.isInBounds(mouseX - this.leftPos, mouseY - this.topPos)) {
                v += button.getHeight() * (this.mousePressed ? 1 : 2);
            }
            context.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos + button.getX(), this.topPos + button.getY(), u, v, button.getWidth(), button.getHeight(), 256 , 256);
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else {
            return this.searchField.charTyped(event);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        this.ignoreTypedCharacter = false;
        if (InputConstants.getKey(event).getNumericKeyValue().isPresent() && this.checkHotbarKeyPressed(event)) {
            this.ignoreTypedCharacter = true;
            return true;
        } else {
            if (this.searchField.keyPressed(event)) {
                return true;
            } else {
                return this.searchField.isFocused() && this.searchField.isVisible() && event.key() != InputConstants.KEY_ESCAPE || super.keyPressed(event);
            }
        }
    }

    protected void superExtractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
    }

    protected void renderCostItem(GuiGraphicsExtractor context, int x, int y) {
        var config = FabricWaystones.CONFIG.teleportation_cost;
        MutableComponent text;
        switch (config.cost_type()) {
            case HEALTH -> {
                context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + 4, 186, 15, 9, 9, 256, 256);
                text = Component.translatable("fwaystones.cost.health");
            }
            case HUNGER -> {
                context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + 4, 177, 24, 9, 9, 256, 256);
                text = Component.translatable("fwaystones.cost.hunger");
            }
            case EXPERIENCE -> {
                context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + 4, 177, 15, 9, 9, 256, 256);
                text = Component.translatable("fwaystones.cost.xp");
            }
            case LEVEL -> {
                context.item(new ItemStack(Items.EXPERIENCE_BOTTLE), x - 4, y);
                text = Component.translatable("fwaystones.cost.level");
            }
            case ITEM -> {
                var item = BuiltInRegistries.ITEM.getValue(Utils.getTeleportCostItem());
                var stack = new ItemStack(item);
                context.item(stack, x - 4, y);
                text = (MutableComponent) item.getName(stack);
            }
            default -> {
                context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + 4, 186, 24, 9, 9, 256, 256);
                text = Component.translatable("fwaystones.cost.free");
            }
        }

        renderCostText(context, x, y, text);
    }

    protected void renderCostText(GuiGraphicsExtractor context, int x, int y, MutableComponent text) {
        renderCostText(context, x, y, text, 0xFF161616);
    }

    protected void renderCostText(GuiGraphicsExtractor context, int x, int y, MutableComponent text, int color) {
        if (!FabricWaystones.CONFIG.teleportation_cost.cost_type().equals(FWConfigModel.CostType.NONE)) {
            text = text.append(Component.literal(": " + FabricWaystones.CONFIG.teleportation_cost.base_cost()));
        }
        context.text(font, text, x + 16, y + 5, color, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        context.text(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
    }


    protected void renderForgetButtons(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y) {
        int n = getDiscoveredCount();
        for (int i = 0; i < 5; ++i) {
            int r = y + i * 18;
            int v = 0;
            if (i >= n) {
                v = 8;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 8 && mouseY < r + 8) {
                v += 8 * (mouseClicked ? 1 : 2);
            }
            context.blit(RenderPipelines.GUI_TEXTURED, texture, x, r, 199, v, 8, 8, 256, 256);
        }
    }

    protected void renderForgetTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y) {
        int n = getDiscoveredCount();
        for (int i = 0; i < n; ++i) {
            int r = y + i * 18;
            if (mouseX < x || mouseY < r || mouseX > x + 8 || mouseY >= r + 8) {
                continue;
            }
            context.setTooltipForNextFrame(font, Component.translatable("fwaystones.gui.forget_tooltip"), mouseX, mouseY);
        }
    }

    protected void renderWaystoneBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int m) {
        for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.imageHeight;
            if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                s += mouseClicked ? 18 : 36;
            }
            context.blit(RenderPipelines.GUI_TEXTURED, texture, x, r - 1, 0, s, 101, 18, 256, 256);
        }
    }

    protected void renderWaystoneTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int m) {
        ArrayList<String> waystones = getDiscoveredWaystones();
        for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            if (mouseX < x || mouseY < r || mouseX >= x + 101 || mouseY >= r + 18) {
                continue;
            }
            var waystoneData = FabricWaystones.WAYSTONE_STORAGE.getWaystoneData(waystones.get(n));
            if (waystoneData == null) {
                continue;
            }
            var startDim = Utils.getDimensionName(minecraft.player.level());
            var endDim = waystoneData.getWorldName();
            List<Component> tooltipContents = new ArrayList<>();
            var cost = Utils.getCost(Vec3.atCenterOf(waystoneData.way_getPos()), minecraft.player.position(), startDim, endDim);
            tooltipContents.add(Component.translatable("fwaystones.gui.cost_tooltip", cost == 0 ? Component.translatable("fwaystones.cost.free").getString() : cost));
            if (minecraft.hasShiftDown()) {
                tooltipContents.add(Component.translatable("fwaystones.gui.dimension_tooltip", waystoneData.getWorldName()));
            }
            context.setComponentTooltipForNextFrame(font, tooltipContents, mouseX, mouseY);
        }
    }

    protected void renderWaystoneNames(GuiGraphicsExtractor context, int x, int y, int m) {
        if (FabricWaystones.WAYSTONE_STORAGE == null)
            return;
        ArrayList<String> waystones = getDiscoveredWaystones();
        for (int n = this.scrollOffset; n < m && n < waystones.size(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;

            String name = FabricWaystones.WAYSTONE_STORAGE.getName(waystones.get(n));
            context.text(font, name, x + 5, r - 1 + 5, 0xFF161616, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        this.mousePressed = true;
        if (event.button() != 0) {
            return super.mouseClicked(event, doubled);
        }
        this.mouseClicked = false;
        if (this.hasWaystones() && canClickWaystones() && tryClick(event.x(), event.y())) {
            return true;
        }
        for (Button guiButton : buttons) {
            if (!guiButton.isVisible() || !guiButton.isInBounds((int) event.x() - this.leftPos, (int) event.y() - this.topPos)) {
                continue;
            }
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            guiButton.onClick();
        }

        return super.mouseClicked(event, doubled);
    }

    protected boolean canClickWaystones() {
        return true;
    }

    protected boolean tryClick(double mouseX, double mouseY) {
        int forgetButtonX = this.leftPos + 24;
        int forgetButtonY = this.topPos + 45;
        int waystoneButtonX = this.leftPos + 36;
        int waystoneButtonY = this.topPos + 40;
        int adjustedScrollOffset = this.scrollOffset + 5;

        int n = getDiscoveredCount();
        for (int currentWaystone = this.scrollOffset; currentWaystone < adjustedScrollOffset && currentWaystone < n; ++currentWaystone) {
            int currentWaystoneOffsetPosition = currentWaystone - this.scrollOffset;
            int forgetButtonStartX = (int) (mouseX - forgetButtonX);
            int forgetButtonStartY = (int) (mouseY - (forgetButtonY + currentWaystoneOffsetPosition * 18));

            int waystoneButtonStartX = (int) (mouseX - waystoneButtonX);
            int waystoneButtonStartY = (int) (mouseY - (waystoneButtonY + currentWaystoneOffsetPosition * 18));
            if (currentWaystoneOffsetPosition < n && forgetButtonStartX >= 0.0D && forgetButtonStartY >= 0.0D && forgetButtonStartX < 8 && forgetButtonStartY < 8 && (this.menu).clickMenuButton(this.minecraft.player, currentWaystone * 2 + 1)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_BREAK, 1.0F));
                this.scrollOffset = Math.max(0, this.scrollOffset - 1);

                ClientPlayNetworking.send(new WaystoneGUISlotClickPacket(menu.containerId, currentWaystone * 2 + 1));

                return true;
            }
            if (menu instanceof WaystoneBlockScreenHandler waystoneBlockScreenHandler && waystoneBlockScreenHandler.getWaystone().equals(getDiscoveredWaystones().get(currentWaystone))) {
                continue;
            }
            if (waystoneButtonStartX >= 0.0D && waystoneButtonStartY >= 0.0D && waystoneButtonStartX < 101.0D && waystoneButtonStartY < 18.0D && (this.menu).clickMenuButton(this.minecraft.player, currentWaystone * 2)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                ClientPlayNetworking.send(new WaystoneGUISlotClickPacket(menu.containerId, currentWaystone * 2));
                return true;
            }
        }

        int i3 = this.leftPos + 141;
        int j3 = this.topPos + 40;
        if (mouseX >= (double) i3 && mouseX < (double) (i3 + 11) && mouseY >= (double) j3 && mouseY < (double) (j3 + 90)) {
            this.mouseClicked = true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            this.scrollAmount = (float) ((double) this.scrollAmount - verticalAmount / (double) i);
            this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5D);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.topPos + 40;
            int j = i + 90;
            this.scrollAmount = ((float) event.y() - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(event, deltaX, deltaY);
        }
    }

    protected boolean hasWaystones() {
        return getDiscoveredCount() > 0;
    }

    protected boolean shouldScroll() {
        return getDiscoveredCount() > 5;
    }

    protected int getMaxScroll() {
        return getDiscoveredCount() - 5;
    }

    protected int getDiscoveredCount() {
        return ((UniversalWaystoneScreenHandler) menu).getWaystonesCount();
    }

    protected ArrayList<String> getDiscoveredWaystones() {
        return ((UniversalWaystoneScreenHandler) menu).getSearchedWaystones();
    }

    protected boolean superMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected boolean superMouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        return super.mouseDragged(event, deltaX, deltaY);
    }

    protected void superResize(int width, int height) {
        super.resize(width, height);
    }

    protected void superOnMouseClick(Slot slot, int invSlot, int clickData, ContainerInput actionType) {
        super.slotClicked(slot, invSlot, clickData, actionType);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.mouseClicked = false;
        this.mousePressed = false;
        return super.mouseReleased(event);
    }

    protected boolean superMouseClicked(MouseButtonEvent event, boolean doubled) {
        return super.mouseClicked(event, doubled);
    }

}
