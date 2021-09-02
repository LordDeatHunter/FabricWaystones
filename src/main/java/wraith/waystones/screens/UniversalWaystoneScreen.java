package wraith.waystones.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;
import wraith.waystones.client.WaystonesClient;

import java.util.ArrayList;

public class UniversalWaystoneScreen extends HandledScreen<ScreenHandler> {

    private final Identifier texture;
    protected final PlayerInventory inventory;
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;
    protected boolean ignoreTypedCharacter;
    private TextFieldWidget searchField;
    protected final ArrayList<Button> buttons = new ArrayList<>();
    protected boolean mousePressed;

    public UniversalWaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Identifier texture, Text title) {
        super(handler, inventory, title);
        this.inventory = inventory;
        this.texture = texture;
        this.backgroundWidth = 177;
        this.backgroundHeight = 140;
        buttons.add(new Button(140, 25, 13, 13, 225, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((UniversalWaystoneScreenHandler)handler).toggleSearchType();
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
                return ((UniversalWaystoneScreenHandler)handler).getSearchTypeTooltip();
            }
        });

    }

    protected boolean searchVisible() {
        return true;
    }

    @Override
    protected void init() {
        super.init();

        this.searchField = new TextFieldWidget(this.textRenderer, this.x + 37, this.y + 27, 93, 10, new TranslatableText("waystone.search")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
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
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5D);
            ((UniversalWaystoneScreenHandler)handler).setFilter(this.searchField != null ? this.searchField.getText() : "");
            ((UniversalWaystoneScreenHandler)handler).filterWaystones();
        });
        this.addSelectableChild(this.searchField);
    }

    @Override
    public void handledScreenTick() {
        if (this.searchField != null) {
            this.searchField.tick();
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
        int k = (int)(41.0F * this.scrollAmount);
        this.drawTexture(matrices, x + 141, y + 40 + k, 177 + (this.shouldScroll() ? 0 : 11), 0, 11, 15);
        int n = this.scrollOffset + 3;
        this.renderWaystoneBackground(matrices, mouseX, mouseY, this.x + 36, this.y + 39, n);
        this.renderForgetButtons(matrices, mouseX, mouseY, this.x + 24, this.y + 45);
        renderButtons(matrices, mouseX, mouseY);
        this.renderCostItem(matrices, this.x + 40, this.y + 100);
        this.renderWaystoneNames(matrices, this.x + 36, this.y + 40, n);
        this.renderWaystoneAmount(matrices, this.x + 10, this.y + 124);
        this.searchField.render(matrices, mouseX, mouseY, delta);
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
        this.textRenderer.draw(matrices, new TranslatableText("waystones.gui.displayed_waystones").append(new LiteralText(" " + this.getDiscoveredCount())), x, y, 0x161616);
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
        MutableText text;
        switch (Config.getInstance().teleportType()) {
            case "hp", "health" -> {
                this.drawTexture(matrices, x, y + 3, 186, 15, 9, 9);
                text = new TranslatableText("waystones.cost.health");
            }
            case "xp", "experience" -> {
                this.drawTexture(matrices, x, y + 3, 177, 15, 9, 9);
                text = new TranslatableText("waystones.cost.xp");
            }
            case "level" -> {
                this.itemRenderer.renderGuiItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), x, y);
                text = new TranslatableText("waystones.cost.level");
            }
            case "item" -> {
                this.itemRenderer.renderGuiItemIcon(new ItemStack(Registry.ITEM.get(Config.getInstance().teleportCostItem())), x, y);
                text = new TranslatableText("waystones.cost.item");
            }
            default -> text = new TranslatableText("waystones.cost.free");
        }

        renderCostText(matrices, x, y, text);
    }

    protected void renderCostText(MatrixStack matrices, int x, int y, MutableText text) {
        this.textRenderer.draw(matrices, text.append(new LiteralText(": " + Config.getInstance().teleportCost())), x + 20, y + 5, 0x161616);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 4210752);
    }


    protected void renderForgetButtons(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y) {
        int n = getDiscoveredCount();
        for (int i = 0; i < 3; ++i) {
            int r = y + i * 18;
            int v = 0;
            if (i >= n) {
                v = 8;
            }
            else if (mouseX >= x && mouseY >= r && mouseX < x + 8 && mouseY < r + 8) {
                v += 8 * (mouseClicked ? 1 : 2);
            }
            this.drawTexture(matrixStack, x, r, 199, v, 8, 8);
        }
    }

    protected void renderWaystoneBackground(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {
        for(int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            this.drawTexture(matrixStack, x, r - 1, 0, s, 101, 18);
        }
    }

    protected void renderWaystoneNames(MatrixStack matrices, int x, int y, int m) {
        ArrayList<String> waystones = getDiscoveredWaystones();
        for(int n = this.scrollOffset; n < m && n < waystones.size(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;

            String name = WaystonesClient.WAYSTONE_STORAGE.getName(waystones.get(n));
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
            if (!guiButton.isVisible() || !guiButton.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
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
        int i1 = this.x + 24;
        int j1 = this.y + 45;
        int i2 = this.x + 36;
        int j2 = this.y + 39;
        int k = this.scrollOffset + 3;

        int n = getDiscoveredCount();
        for(int l = this.scrollOffset; l < k; ++l) {
            int m = l - this.scrollOffset;
            double x1 = mouseX - (double)(i1);
            double y1 = mouseY - (double)(j1 + m * 18);

            double x2 = mouseX - (double)(i2);
            double y2 = mouseY - (double)(j2 + m * 18);
            if (m < n && x1 >= 0.0D && y1 >= 0.0D && x1 < 8 && y1 < 8 && (this.handler).onButtonClick(this.client.player, l * 2 + 1)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                this.scrollOffset = Math.max(0, this.scrollOffset - 1);

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", l * 2 + 1);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer()).writeNbt(tag);

                ClientPlayNetworking.send(Utils.ID("waystone_gui_slot_click"), packet);

                return true;
            }
            if (x2 >= 0.0D && y2 >= 0.0D && x2 < 101.0D && y2 < 18.0D && (this.handler).onButtonClick(this.client.player, l * 2)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                NbtCompound tag = new NbtCompound();
                tag.putInt("sync_id", handler.syncId);
                tag.putInt("clicked_slot", l * 2);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer()).writeNbt(tag);

                ClientPlayNetworking.send(Utils.ID("waystone_gui_slot_click"), packet);
                return true;
            }
        }

        int i3 = this.x + 141;
        int j3 = this.y + 40;
        if (mouseX >= (double)i3 && mouseX < (double)(i3 + 11) && mouseY >= (double)j3 && mouseY < (double)(j3 + 54)) {
            this.mouseClicked = true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            this.scrollAmount = (float)((double)this.scrollAmount - amount / (double)i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)i) + 0.5D);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 40;
            int j = i + 54;
            this.scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    protected boolean hasWaystones() {
        return getDiscoveredCount() > 0;
    }

    protected boolean shouldScroll() {
        return getDiscoveredCount() > 3;
    }

    protected int getMaxScroll() {
        return getDiscoveredCount() - 3;
    }

    protected int getDiscoveredCount() {
        return ((UniversalWaystoneScreenHandler)handler).getWaystonesCount();
    }

    protected ArrayList<String> getDiscoveredWaystones() {
        return ((UniversalWaystoneScreenHandler)handler).getSearchedWaystones();
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
        this.mousePressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected boolean superMouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
