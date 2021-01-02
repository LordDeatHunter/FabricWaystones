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
import net.minecraft.util.registry.Registry;
import wraith.waystones.Config;
import wraith.waystones.Utils;
import wraith.waystones.Waystone;
import wraith.waystones.Waystones;

import java.util.Objects;


public class WaystoneScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/container/waystone.png");
    private boolean ignoreTypedCharacter;
    private TextFieldWidget nameInputField;

    public WaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);
        this.backgroundWidth = 177;
        this.backgroundHeight = 110;
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
        this.nameInputField = new TextFieldWidget(this.textRenderer, this.x + 37, this.y + 23, 93, 9, new TranslatableText("waystone.rename"));
        this.nameInputField.setMaxLength(16);
        this.nameInputField.setHasBorder(false);
        this.nameInputField.setEditableColor(0xffffff);
        this.nameInputField.setVisible(true);
        this.nameInputField.setFocusUnlocked(true);
        this.nameInputField.setSelected(true);
        this.nameInputField.setText(((WaystoneScreenHandler)handler).getName());
        this.children.add(this.nameInputField);
        this.playerInventoryTitleY = this.backgroundHeight - 92;
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
        if (waystone == null) {
            return;
        }
        String oldName = waystone.name;
        if (fieldName == null) {
            fieldName = "";
        }

        String newName = Utils.generateWaystoneName(fieldName);
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        CompoundTag tag = new CompoundTag();
        tag.putString("old_name", oldName);
        tag.putString("new_name", newName);
        data.writeCompoundTag(tag);
        Waystones.WAYSTONE_DATABASE.renameWaystone(oldName, newName);
        ClientPlayNetworking.send(Utils.ID("rename_waystone"), data);
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        super.drawBackground(matrices, delta, mouseX, mouseY);
        this.nameInputField.render(matrices, mouseX, mouseY, delta);
    }


    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        super.onMouseClick(slot, invSlot, clickData, actionType);
        this.nameInputField.setCursorToEnd();
        this.nameInputField.setSelectionEnd(0);
    }



}