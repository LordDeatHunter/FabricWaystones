package wraith.waystones.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import wraith.waystones.Utils;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;

import java.util.Objects;


public class WaystoneScreen extends HandledScreen<ScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(Waystones.MOD_ID, "textures/gui/container/waystone.png");
    private float scrollAmount;
    private boolean mouseClicked;
    private int scrollOffset;
    private boolean ignoreTypedCharacter;
    private TextFieldWidget nameInputField;

    public WaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 175;
        this.backgroundHeight = 185;
        this.playerInventoryTitleY = this.backgroundHeight - 96;
    }

    @Override
    public void tick() {
        if (this.nameInputField != null) {
            this.nameInputField.tick();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        this.nameInputField = new TextFieldWidget(this.textRenderer, this.x + 31, this.y + 23, 93, 9, new TranslatableText("waystone.rename"));
        this.nameInputField.setMaxLength(16);
        this.nameInputField.setHasBorder(false);
        this.nameInputField.setVisible(false);
        this.nameInputField.setEditableColor(0xffffff);
        this.nameInputField.setVisible(true);
        this.nameInputField.setFocusUnlocked(false);
        this.nameInputField.setSelected(true);
        this.nameInputField.setText(((WaystoneScreenHandler)handler).getName());
        this.children.add(this.nameInputField);
        this.playerInventoryTitleY = this.backgroundHeight - 96;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.nameInputField.getText();
        this.init(client, width, height);
        this.nameInputField.setText(string);
        if (!this.nameInputField.getText().isEmpty()) {
            this.rename();
        }

    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (this.ignoreTypedCharacter || chr == '"') {
            return false;
        } else {
            String string = this.nameInputField.getText();
            if (this.nameInputField.charTyped(chr, keyCode)) {
                if (!Objects.equals(string, this.nameInputField.getText())) {
                    this.rename();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        } else {
            String string = this.nameInputField.getText();
            if (this.nameInputField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(string, this.nameInputField.getText())) {
                    this.rename();
                }

                return true;
            } else {
                return this.nameInputField.isFocused() && this.nameInputField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    private void rename() {
        String fieldName = this.nameInputField.getText();
        Waystone waystone = Waystones.WAYSTONE_DATABASE.getWaystone(((WaystoneScreenHandler)handler).getPos(), ((WaystoneScreenHandler)handler).getWorld());
        if (waystone == null)return;
        String oldName = waystone.name;
        if (fieldName == null)
            fieldName = "";

        String newName = Utils.generateWaystoneName(fieldName);
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        CompoundTag tag = new CompoundTag();
        tag.putString("old_name", oldName);
        tag.putString("new_name", newName);
        data.writeCompoundTag(tag);
        Waystones.WAYSTONE_DATABASE.renameWaystone(oldName, newName);
        ClientSidePacketRegistry.INSTANCE.sendToServer(new Identifier(Waystones.MOD_ID, "rename_waystone"), data);
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }


    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int k = (int)(41.0F * this.scrollAmount);
        this.drawTexture(matrices, i + 134, j + 35 + k, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
        int l = this.x + 30;
        int m = this.y + 34;
        int n = this.scrollOffset + 3;
        this.renderWaystoneBackground(matrices, mouseX, mouseY, l, m, n);
        this.renderWaystoneNames(matrices, l, m, n);
        this.nameInputField.render(matrices, mouseX, mouseY, delta);
    }

    private void renderWaystoneBackground(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                if (mouseClicked)
                    s += 18;
                else s += 36;
            }
            this.drawTexture(matrixStack, k, r - 1, 0, s, 101, 18);
        }
    }

    private void renderWaystoneNames(MatrixStack matrices, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;

            this.textRenderer.draw(matrices, Waystones.WAYSTONE_DATABASE.getDiscoveredWaystoneNames(playerInventory.player).get(n), k + 5, r - 1 + 5, 0x161616);
        }

    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
        this.nameInputField.setCursorToEnd();
        this.nameInputField.setSelectionEnd(0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        if (this.hasWaystones()) {
            int i = this.x + 30;
            int j = this.y + 34;
            int k = this.scrollOffset + 3;

            for(int l = this.scrollOffset; l < k; ++l) {
                int m = l - this.scrollOffset;
                double d = mouseX - (double)(i);
                double e = mouseY - (double)(j + m * 18);
                if (d >= 0.0D && e >= 0.0D && d < 101.0D && e < 18.0D && (this.handler).onButtonClick(this.client.player, l)) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F));
                    this.client.interactionManager.clickButton((this.handler).syncId, l);
                    return true;
                }
            }

            i = this.x + 134;
            j = this.y + 18;
            if (mouseX >= (double)i && mouseX < (double)(i + 12) && mouseY >= (double)j && mouseY < (double)(j + 54)) {
                this.mouseClicked = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + 54;
            this.scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
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

    private boolean hasWaystones() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) > 0;
    }

    private boolean shouldScroll() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) > 3;
    }

    protected int getMaxScroll() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) - 3;
    }


}