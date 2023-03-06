package wraith.fwaystones.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.util.WaystonePacketHandler;

import java.util.ArrayList;
import java.util.List;

public class UniversalWaystoneScreen extends HandledScreen<ScreenHandler> {

    protected final PlayerInventory inventory;
    protected final ArrayList<Button> buttons = new ArrayList<>();
    private final Identifier texture;
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;
    protected boolean ignoreTypedCharacter;
    protected boolean mousePressed;
    private TextFieldWidget searchField;

    public UniversalWaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Identifier texture, Text title) {
        super(handler, inventory, title);
        this.inventory = inventory;
        this.texture = texture;
        this.backgroundWidth = 177;
        this.backgroundHeight = 176;
        buttons.add(new Button(140, 25, 13, 13, 225, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((UniversalWaystoneScreenHandler) handler).toggleSearchType();
                searchField.setTextFieldFocused(((PlayerEntityMixinAccess) client.player).autofocusWaystoneFields());
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
            public Text tooltip() {
                return ((UniversalWaystoneScreenHandler) handler).getSearchTypeTooltip();
            }
        });

        //Autoselect search lock
        buttons.add(new ToggleableButton(24, 26, 8, 11, 177, 33, 185, 33) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess) inventory.player).autofocusWaystoneFields();
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
                ((PlayerEntityMixinAccess) inventory.player).toggleAutofocusWaystoneFields();
                setupTooltip();
            }

            private void setupTooltip() {
                this.tooltip = this.toggled
                    ? Text.translatable("fwaystones.config.tooltip.unlock_search")
                    : Text.translatable("fwaystones.config.tooltip.lock_search");
            }
        });

        setupButtons();
    }

    @Override
    public void close() {
        super.close();
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeNbt(((PlayerEntityMixinAccess) inventory.player).toTagW(new NbtCompound()));
        ClientPlayNetworking.send(WaystonePacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
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

        this.searchField = new TextFieldWidget(this.textRenderer, this.x + 37, this.y + 27, 93, 10, Text.literal("")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
                if (bl && button == 1) {
                    this.setText("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        this.searchField.setMaxLength(16);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setVisible(true);
        this.searchField.setDrawsBackground(false);
        this.searchField.setFocusUnlocked(true);
        this.searchField.setText("");
        this.searchField.setChangedListener((s) -> {
            this.scrollAmount = 0;
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5D);
            ((UniversalWaystoneScreenHandler) handler).setFilter(this.searchField != null ? this.searchField.getText() : "");
            ((UniversalWaystoneScreenHandler) handler).filterWaystones();
        });
        this.addSelectableChild(this.searchField);
    }

    @Override
    public void handledScreenTick() {
        if (this.searchField != null && this.searchField.isVisible()) {
            this.searchField.tick();
            if (((PlayerEntityMixinAccess) client.player).autofocusWaystoneFields()) {
                this.searchField.setTextFieldFocused(true);
            }
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.searchField.getText();
        this.init(client, width, height);
        this.searchField.setText(string);
        super.resize(client, width, height);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int k = (int) (75.0F * this.scrollAmount);
        this.drawTexture(matrices, x + 141, y + 40 + k, 177 + (this.shouldScroll() ? 0 : 11), 0, 11, 15);
        int n = this.scrollOffset + 5;
        // TODO: Merge some of these
        this.renderWaystoneBackground(matrices, mouseX, mouseY, this.x + 36, this.y + 39, n);
        this.renderForgetButtons(matrices, mouseX, mouseY, this.x + 24, this.y + 45);
        renderButtons(matrices, mouseX, mouseY);
        this.renderCostItem(matrices, this.x + 40, this.y + 136);
        this.renderWaystoneNames(matrices, this.x + 36, this.y + 40, n);
        this.renderWaystoneTooltips(matrices, mouseX, mouseY, this.x + 36, this.y + 39, n);
        this.renderWaystoneAmount(matrices, this.x + 10, this.y + 160);
        this.searchField.render(matrices, mouseX, mouseY, delta);
        this.renderForgetTooltips(matrices, mouseX, mouseY, this.x + 24, this.y + 45);
        this.renderButtonTooltips(matrices, mouseX, mouseY);
    }

    protected void renderButtonTooltips(MatrixStack matrices, int mouseX, int mouseY) {
        for (Button button : buttons) {
            if (!button.isVisible() || !button.hasToolTip() || !button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                continue;
            }
            this.renderTooltip(matrices, button.tooltip(), mouseX, mouseY);
        }
    }

    private void renderWaystoneAmount(MatrixStack matrices, int x, int y) {
        this.textRenderer.draw(matrices, Text.translatable("fwaystones.gui.displayed_waystones", this.getDiscoveredCount()), x, y, 0x161616);
    }

    protected void renderButtons(MatrixStack matrices, int mouseX, int mouseY) {
        for (Button button : buttons) {
            if (!button.isVisible()) {
                continue;
            }
            int u = button.getU();
            int v = button.getV();
            if (button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v += button.getHeight() * (this.mousePressed ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + button.getX(), this.y + button.getY(), u, v, button.getWidth(), button.getHeight());
        }
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else {
            return this.searchField.charTyped(chr, keyCode);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        } else {
            if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            } else {
                return this.searchField.isFocused() && this.searchField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void renderCostItem(MatrixStack matrices, int x, int y) {
        var config = FabricWaystones.CONFIG.teleportation_cost;
        MutableText text;
        switch (config.cost_type()) {
            case HEALTH -> {
                this.drawTexture(matrices, x + 4, y + 4, 186, 15, 9, 9);
                text = Text.translatable("fwaystones.cost.health");
            }
            case HUNGER -> {
                this.drawTexture(matrices, x + 4, y + 4, 177, 24, 9, 9);
                text = Text.translatable("fwaystones.cost.hunger");
            }
            case EXPERIENCE -> {
                this.drawTexture(matrices, x + 4, y + 4, 177, 15, 9, 9);
                text = Text.translatable("fwaystones.cost.xp");
            }
            case LEVEL -> {
                this.itemRenderer.renderGuiItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), x, y);
                text = Text.translatable("fwaystones.cost.level");
            }
            case ITEM -> {
                var item = Registry.ITEM.get(Utils.getTeleportCostItem());
                this.itemRenderer.renderGuiItemIcon(new ItemStack(item), x, y);
                text = (MutableText) item.getName();
            }
            default -> text = Text.translatable("fwaystones.cost.free");
        }

        renderCostText(matrices, x, y, text);
    }

    protected void renderCostText(MatrixStack matrices, int x, int y, MutableText text) {
        renderCostText(matrices, x, y, text, 0x161616);
    }

    protected void renderCostText(MatrixStack matrices, int x, int y, MutableText text, int color) {
        this.textRenderer.draw(matrices, text.append(Text.literal(": " + FabricWaystones.CONFIG.teleportation_cost.base_cost())), x + 20, y + 5, color);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float) this.titleX, (float) this.titleY, 4210752);
    }


    protected void renderForgetButtons(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y) {
        int n = getDiscoveredCount();
        for (int i = 0; i < 5; ++i) {
            int r = y + i * 18;
            int v = 0;
            if (i >= n) {
                v = 8;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 8 && mouseY < r + 8) {
                v += 8 * (mouseClicked ? 1 : 2);
            }
            this.drawTexture(matrixStack, x, r, 199, v, 8, 8);
        }
    }

    protected void renderForgetTooltips(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y) {
        int n = getDiscoveredCount();
        for (int i = 0; i < n; ++i) {
            int r = y + i * 18;
            if (mouseX < x || mouseY < r || mouseX > x + 8 || mouseY >= r + 8) {
                continue;
            }
            this.renderTooltip(matrixStack, Text.translatable("fwaystones.gui.forget_tooltip"), mouseX, mouseY);
        }
    }

    protected void renderWaystoneBackground(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {
        for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                s += mouseClicked ? 18 : 36;
            }
            this.drawTexture(matrixStack, x, r - 1, 0, s, 101, 18);
        }
    }

    protected void renderWaystoneTooltips(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {
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
            var startDim = Utils.getDimensionName(client.player.world);
            var endDim = waystoneData.getWorldName();
            List<Text> tooltipContents = new ArrayList<>();
            tooltipContents.add(Text.translatable("fwaystones.gui.cost_tooltip", Utils.getCost(Vec3d.ofCenter(waystoneData.way_getPos()), client.player.getPos(), startDim, endDim)));
            if (hasShiftDown()) {
                tooltipContents.add(Text.translatable("fwaystones.gui.dimension_tooltip", waystoneData.getWorldName()));
            }
            this.renderTooltip(matrixStack, tooltipContents, mouseX, mouseY);
        }
    }

    protected void renderWaystoneNames(MatrixStack matrices, int x, int y, int m) {
        if (FabricWaystones.WAYSTONE_STORAGE == null)
            return;
        ArrayList<String> waystones = getDiscoveredWaystones();
        for (int n = this.scrollOffset; n < m && n < waystones.size(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;

            String name = FabricWaystones.WAYSTONE_STORAGE.getName(waystones.get(n));
            this.textRenderer.draw(matrices, name, x + 5f, r - 1 + 5f, 0x161616);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mousePressed = true;
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        this.mouseClicked = false;
        if (this.hasWaystones() && canClickWaystones() && tryClick(mouseX, mouseY)) {
            return true;
        }
        for (Button guiButton : buttons) {
            if (!guiButton.isVisible() || !guiButton.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
                continue;
            }
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            guiButton.onClick();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean canClickWaystones() {
        return true;
    }

    protected boolean tryClick(double mouseX, double mouseY) {
        int forgetButtonX = this.x + 24;
        int forgetButtonY = this.y + 45;
        int waystoneButtonX = this.x + 36;
        int waystoneButtonY = this.y + 40;
        int adjustedScrollOffset = this.scrollOffset + 5;

        int n = getDiscoveredCount();
        for (int currentWaystone = this.scrollOffset; currentWaystone < adjustedScrollOffset && currentWaystone < n; ++currentWaystone) {
            int currentWaystoneOffsetPosition = currentWaystone - this.scrollOffset;
            int forgetButtonStartX = (int) (mouseX - forgetButtonX);
            int forgetButtonStartY = (int) (mouseY - (forgetButtonY + currentWaystoneOffsetPosition * 18));

            int waystoneButtonStartX = (int) (mouseX - waystoneButtonX);
            int waystoneButtonStartY = (int) (mouseY - (waystoneButtonY + currentWaystoneOffsetPosition * 18));
            if (currentWaystoneOffsetPosition < n && forgetButtonStartX >= 0.0D && forgetButtonStartY >= 0.0D && forgetButtonStartX < 8 && forgetButtonStartY < 8 && (this.handler).onButtonClick(this.client.player, currentWaystone * 2 + 1)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                this.scrollOffset = Math.max(0, this.scrollOffset - 1);

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", currentWaystone * 2 + 1);
                PacketByteBuf packet = PacketByteBufs.create().writeNbt(tag);

                ClientPlayNetworking.send(WaystonePacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);

                return true;
            }
            if (handler instanceof WaystoneBlockScreenHandler waystoneBlockScreenHandler && waystoneBlockScreenHandler.getWaystone().equals(getDiscoveredWaystones().get(currentWaystone))) {
                continue;
            }
            if (waystoneButtonStartX >= 0.0D && waystoneButtonStartY >= 0.0D && waystoneButtonStartX < 101.0D && waystoneButtonStartY < 18.0D && (this.handler).onButtonClick(this.client.player, currentWaystone * 2)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", currentWaystone * 2);
                PacketByteBuf packet = PacketByteBufs.create().writeNbt(tag);

                ClientPlayNetworking.send(WaystonePacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);
                return true;
            }
        }

        int i3 = this.x + 141;
        int j3 = this.y + 40;
        if (mouseX >= (double) i3 && mouseX < (double) (i3 + 11) && mouseY >= (double) j3 && mouseY < (double) (j3 + 90)) {
            this.mouseClicked = true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            this.scrollAmount = (float) ((double) this.scrollAmount - amount / (double) i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5D);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 40;
            int j = i + 90;
            this.scrollAmount = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
        return ((UniversalWaystoneScreenHandler) handler).getWaystonesCount();
    }

    protected ArrayList<String> getDiscoveredWaystones() {
        return ((UniversalWaystoneScreenHandler) handler).getSearchedWaystones();
    }

    protected boolean superMouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    protected boolean superMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    protected void superResize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
    }

    protected void superOnMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        this.mousePressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected boolean superMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
