package wraith.waystones.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import wraith.waystones.PlayerEntityMixinAccess;
import wraith.waystones.Utils;
import wraith.waystones.WaystonesClient;

import java.util.ArrayList;
import java.util.UUID;

public class WaystoneScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/waystone.png");
    private static final Identifier CONFIG_TEXTURE = Utils.ID("textures/gui/waystone_config.png");

    private TextFieldWidget nameField;
    private final ArrayList<Button> buttons = new ArrayList<>();
    protected boolean mousePressed;

    private Button configPage = new Button(156, -17, 18, 18, 210, 0) {
        @Override
        public void onClick() {
            if (!isVisible()) {
                return;
            }
            page = Page.CONFIG;
            backgroundHeight = 125;
            for (Button button : buttons) {
                button.setup();
            }
        }
        @Override
        public boolean isVisible() {
            return page == Page.WAYSTONES;
        }

        @Override
        public void setup() {
            this.tooltip = new TranslatableText("waystones.config.tooltip.config");
        }
    };

    private Page page = Page.WAYSTONES;

    private enum Page {
        WAYSTONES,
        CONFIG
    }

    public WaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);

        buttons.add(configPage);

        buttons.add(new Button(156, -17, 18, 18, 178, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                page = Page.WAYSTONES;
                backgroundHeight = 140;
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.back");
            }
        });

        //Reset name
        buttons.add(new Button(8, 103, 13, 13, 178, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setText("");
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.delete_name");
            }
        });

        //Set name
        buttons.add(new ToggleableButton(128, 103, 13, 13, 191, 54, 217, 54) {
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.set_name");
                boolean settable = !((WaystoneScreenHandler)handler).getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                rename();
                boolean settable = !((WaystoneScreenHandler)handler).getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }

        });

        //Randomize name
        buttons.add(new Button(143, 103, 13, 13, 204, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setText(Utils.generateWaystoneName(""));
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.randomize_name");
            }
        });

        //Global Toggle
        buttons.add(new ToggleableButton(8, 48, 17, 17, 213, 0, 196, 0) {

            @Override
            public void setup() {
                this.toggled = ((WaystoneScreenHandler)handler).isGlobal();
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_is_global");
            }
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((WaystoneScreenHandler)handler).toggleGlobal();
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }
        });

        //View discovered
        buttons.add(new ToggleableButton(8, 11, 13, 13, 178, 54, 191, 54) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess)playerInventory.player).shouldViewDiscoveredWaystones();
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_discovered_view");
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess)playerInventory.player).toggleViewDiscoveredWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                packet.writeNbt(((PlayerEntityMixinAccess)playerInventory.player).toTagW(new NbtCompound()));
                ClientPlayNetworking.send(Utils.ID("sync_player_from_client"), packet);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

        });

        //View global
        buttons.add(new ToggleableButton(8, 27, 13, 13, 178, 54, 191, 54) {
            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.toggle_global_view");
                this.toggled = ((PlayerEntityMixinAccess)playerInventory.player).shouldViewGlobalWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess)playerInventory.player).toggleViewGlobalWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                packet.writeNbt(((PlayerEntityMixinAccess)playerInventory.player).toTagW(new NbtCompound()));
                ClientPlayNetworking.send(Utils.ID("sync_player_from_client"), packet);
            }
            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }
        });

        //Revoke ownership
        buttons.add(new Button(30, 48, 17, 17, 230, 0) {
            @Override
            public void onClick() {
                super.onClick();
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                NbtCompound tag = new NbtCompound();
                tag.putString("waystone_hash", ((WaystoneScreenHandler)handler).getWaystone());
                UUID owner = ((WaystoneScreenHandler)handler).getOwner();
                if (owner != null) {
                    tag.putUuid("waystone_owner", owner);
                }
                packet.writeNbt(tag);
                ClientPlayNetworking.send(Utils.ID("remove_waystone_owner"), packet);
                ((WaystoneScreenHandler)handler).removeOwner();
            }
            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG && ((WaystoneScreenHandler)handler).hasOwner();
            }

            @Override
            public void setup() {
                this.tooltip = new TranslatableText("waystones.config.tooltip.revoke_ownership");
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new TextFieldWidget(this.textRenderer, this.x + 28, this.y + 106, 93, 10, new TranslatableText("waystone.rename")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
                if (bl && button == 1) {
                    this.setText("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
            @Override
            public boolean isVisible() {
                return canEdit();
            }
            @Override
            public boolean changeFocus(boolean lookForwards) {
                return isVisible() && super.changeFocus(lookForwards);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return isVisible() && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            }
        };
        this.nameField.setMaxLength(16);
        this.nameField.setEditableColor(0xFFFFFF);
        this.nameField.setDrawsBackground(false);
        this.nameField.setFocusUnlocked(true);
        String waystone = WaystonesClient.WAYSTONE_STORAGE.getName(((WaystoneScreenHandler)handler).getWaystone());
        this.nameField.setText(waystone == null ? "" : waystone);
        this.nameField.setChangedListener((s) -> {
            boolean settable = !((WaystoneScreenHandler)handler).getName().equals(s);
            ToggleableButton button = ((ToggleableButton)buttons.get(3));
            if (button.isToggled() == settable) {
                button.toggle();
            }
        });
        this.children.add(this.nameField);
    }

    private boolean canEdit() {
        return ((WaystoneScreenHandler)handler).isOwner(playerInventory.player) || playerInventory.player.hasPermissionLevel(2);
    }

    @Override
    public void tick() {
        if (page == Page.WAYSTONES) {
            super.tick();
        } else if (this.nameField != null && this.nameField.isVisible()) {
            this.nameField.tick();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if (page == Page.WAYSTONES) {
            super.resize(client, width, height);
        } else {
            String string = this.nameField.getText();
            this.init(client, width, height);
            this.nameField.setText(string);
        }
        super.superResize(client, width, height);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (page == Page.WAYSTONES) {
            return super.charTyped(chr, keyCode);
        } else {
            if (this.ignoreTypedCharacter) {
                return false;
            } else {
                return this.nameField.isVisible() && this.nameField.charTyped(chr, keyCode);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (page == Page.WAYSTONES) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            this.ignoreTypedCharacter = false;
            if (InputUtil.fromKeyCode(keyCode, scanCode).method_30103().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                if (this.nameField.isVisible() && this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                } else {
                    return this.nameField.isVisible() && this.nameField.isFocused() && this.nameField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            super.drawBackground(matrices, delta, mouseX, mouseY);
        } else {
            this.renderBackground(matrices);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.client.getTextureManager().bindTexture(CONFIG_TEXTURE);
            this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            if (canEdit()) {
                this.drawTexture(matrices, x + 23, y + 103, 0, 125, 103, 13);
            }
            renderButtons(matrices, mouseX, mouseY);
            renderButtonText(matrices);
            String owner = ((WaystoneScreenHandler) handler).getOwnerName();
            Text ownerText;
            if (owner == null || "".equals(owner)) {
                ownerText = new TranslatableText("waystones.config.no_owner");
            } else {
                ownerText = new LiteralText(owner);
            }
            this.textRenderer.draw(matrices, new TranslatableText("waystones.config.owner").append(new LiteralText(": ").append(ownerText)), this.x + 10, this.y + 75, 0x161616);
            if (this.nameField.isVisible()) {
                this.nameField.render(matrices, mouseX, mouseY, delta);
            }

            this.renderButtonTooltips(matrices, mouseX, mouseY);
        }
    }

    private void renderButtonText(MatrixStack matrices) {
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_discovered"), this.x + 25, this.y + 14, 0x161616);
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_global"), this.x + 25, this.y + 30, 0x161616);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            this.textRenderer.draw(matrices, ((WaystoneScreenHandler)handler).getName(), this.titleX, this.titleY, 4210752);
        }
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (page == Page.WAYSTONES) {
            super.onMouseClick(slot, invSlot, clickData, actionType);
        } else {
            super.superOnMouseClick(slot, invSlot, clickData, actionType);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mousePressed = true;
        if (page == Page.WAYSTONES) {
            if (configPage.isVisible() && configPage.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                configPage.onClick();
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
        } else {
            for (Button guiButton : buttons) {
                if (!guiButton.isVisible() || !guiButton.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                    continue;
                }
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                guiButton.onClick();
            }
        }
        return super.superMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mousePressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (page == Page.WAYSTONES) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        } else {
            return super.superMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (page == Page.WAYSTONES) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        } else {
            return super.superMouseScrolled(mouseX, mouseY, amount);
        }
    }

    @Override
    public void renderButtons(MatrixStack matrices, int mouseX, int mouseY) {
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

    private void renderButtonTooltips(MatrixStack matrices, int mouseX, int mouseY) {
        for (Button button : buttons) {
            if (!button.isVisible() || !button.hasToolTip() || !button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                continue;
            }
            this.renderTooltip(matrices, button.tooltip(), mouseX, mouseY);
        }
    }

    private void rename() {
        if (!canEdit()) {
            return;
        }
        String name = this.nameField.getText();
        String hash = ((WaystoneScreenHandler)handler).getWaystone();
        UUID owner = ((WaystoneScreenHandler)handler).getOwner();

        if (name == null) {
            name = "";
        }
        ((WaystoneScreenHandler)handler).setName(name);

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        NbtCompound tag = new NbtCompound();
        tag.putString("waystone_name", name);
        tag.putString("waystone_hash", hash);
        if (owner != null) {
            tag.putUuid("waystone_owner", owner);
        }
        data.writeNbt(tag);

        ClientPlayNetworking.send(Utils.ID("rename_waystone"), data);
    }

}