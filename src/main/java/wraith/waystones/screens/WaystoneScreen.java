package wraith.waystones.screens;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
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

    private Button configPage = new Button(156, 4, 18, 18, 210, 0) {
        @Override
        public void onClick() {
            page = Page.CONFIG;
            for (Button button : buttons) {
                button.setup();
            }
        }
    };
    private Button waystonePage = new Button(156, 4, 18, 18, 178, 0) {
        @Override
        public void onClick() {
            page = Page.WAYSTONES;
            ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
        }
    };

    private Page page = Page.WAYSTONES;

    private enum Page {
        WAYSTONES,
        CONFIG
    }

    public WaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);

        //Reset name
        buttons.add(new Button(5, 17, 13, 13, 178, 54) {
            @Override
            public void onClick() {
                if (!((WaystoneScreenHandler)handler).isOwner(playerInventory.player)) {
                    return;
                }
                super.onClick();
                nameField.setText("");
            }
        });

        //Set name
        buttons.add(new ToggleableButton(125, 17, 13, 13, 191, 54, 217, 54) {
            @Override
            public void setup() {
                boolean settable = !((WaystoneScreenHandler)handler).getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }

            @Override
            public void onClick() {
                if (!((WaystoneScreenHandler)handler).isOwner(playerInventory.player)) {
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
        buttons.add(new Button(140, 17, 13, 13, 204, 54) {
            @Override
            public void onClick() {
                if (!((WaystoneScreenHandler)handler).isOwner(playerInventory.player)) {
                    return;
                }
                super.onClick();
                nameField.setText(Utils.generateWaystoneName(""));
            }
        });

        //Global Toggle
        buttons.add(new ToggleableButton(8, 36, 17, 17, 213, 0, 196, 0) {
            @Override
            public void setup() {
                this.toggled = ((WaystoneScreenHandler)handler).isGlobal();
            }

            @Override
            public void onClick() {
                if (!((WaystoneScreenHandler)handler).isOwner(playerInventory.player)) {
                    return;
                }
                super.onClick();
                ((WaystoneScreenHandler)handler).toggleGlobal();
            }
        });

        //View discovered
        buttons.add(new ToggleableButton(8, 60, 13, 13, 178, 54, 191, 54) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess)playerInventory.player).shouldViewDiscoveredWaystones();
            }

            @Override
            public void onClick() {
                super.onClick();
                ((PlayerEntityMixinAccess)playerInventory.player).toggleViewDiscoveredWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                packet.writeCompoundTag(((PlayerEntityMixinAccess)playerInventory.player).toTagW(new CompoundTag()));
                ClientPlayNetworking.send(Utils.ID("sync_player_from_client"), packet);
            }
        });

        //View global
        buttons.add(new ToggleableButton(8, 76, 13, 13, 178, 54, 191, 54) {
            @Override
            public void setup() {
                this.toggled = ((PlayerEntityMixinAccess)playerInventory.player).shouldViewGlobalWaystones();
            }

            @Override
            public void onClick() {
                super.onClick();
                ((PlayerEntityMixinAccess)playerInventory.player).toggleViewGlobalWaystones();
                ((UniversalWaystoneScreenHandler)handler).updateWaystones(playerInventory.player);
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                packet.writeCompoundTag(((PlayerEntityMixinAccess)playerInventory.player).toTagW(new CompoundTag()));
                ClientPlayNetworking.send(Utils.ID("sync_player_from_client"), packet);
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new TextFieldWidget(this.textRenderer, this.x + 25, this.y + 20, 93, 10, new TranslatableText("waystone.rename")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
                if (bl && button == 1) {
                    this.setText("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        this.nameField.setMaxLength(16);
        this.nameField.setEditableColor(0xFFFFFF);
        this.nameField.setVisible(((WaystoneScreenHandler)handler).isOwner(playerInventory.player));
        this.nameField.setDrawsBackground(false);
        this.nameField.setFocusUnlocked(true);
        String waystone = WaystonesClient.WAYSTONE_STORAGE.getName(((WaystoneScreenHandler)handler).getWaystone());
        this.nameField.setText(waystone == null ? "" : waystone);
        this.nameField.setChangedListener((s) -> {
            boolean settable = !((WaystoneScreenHandler)handler).getName().equals(s);
            ToggleableButton button = ((ToggleableButton)buttons.get(1));
            if (button.isToggled() == settable) {
                button.toggle();
            }
        });
        this.children.add(this.nameField);
    }

    @Override
    public void tick() {
        if (page == Page.WAYSTONES) {
            super.tick();
        } else if (this.nameField != null) {
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
                return this.nameField.charTyped(chr, keyCode);
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
                if (this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                } else {
                    return this.nameField.isFocused() && this.nameField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
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
            renderButtons(matrices, mouseX, mouseY);
            renderButtonText(matrices);
            String owner = ((WaystoneScreenHandler) handler).getOwnerName();
            Text ownerText;
            if (owner == null || "".equals(owner)) {
                ownerText = new TranslatableText("waystones.config.no_owner");
            } else {
                ownerText = new LiteralText(owner);
            }
            this.textRenderer.draw(matrices, new TranslatableText("waystones.config.owner").append(new LiteralText(": ").append(ownerText)), this.x + 10, this.y + 110, 0x161616);

            this.nameField.render(matrices, mouseX, mouseY, delta);
        }
    }

    private void renderButtonText(MatrixStack matrices) {
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.global"), this.x + 29, this.y + 42, 0x161616);
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_discovered"), this.x + 29, this.y + 64, 0x161616);
        this.textRenderer.draw(matrices, new TranslatableText("waystones.config.view_global"), this.x + 29, this.y + 78, 0x161616);
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
            this.nameField.setCursorToEnd();
            this.nameField.setSelectionEnd(0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mousePressed = true;
        if (page == Page.WAYSTONES) {
            if (configPage.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                configPage.onClick();
            } else {
                return super.mouseClicked(mouseX, mouseY, button);
            }
        } else {
            if (waystonePage.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                waystonePage.onClick();
            }
            for (Button guiButton : buttons) {
                if (!guiButton.isInBounds((int)mouseX - this.x, (int)mouseY - this.y)) {
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
        if (page == Page.WAYSTONES) {
            int u = configPage.getU();
            int v = configPage.getV();
            if (configPage.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v += configPage.getHeight() * (this.mousePressed ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + configPage.getX(), this.y + configPage.getY(), u, v, configPage.getWidth(), configPage.getHeight());
        } else {
            int u = waystonePage.getU();
            int v = waystonePage.getV();
            if (waystonePage.isInBounds(mouseX - this.x, mouseY - this.y)) {
                v += waystonePage.getHeight() * (this.mousePressed ? 1 : 2);
            }
            this.drawTexture(matrices, this.x + waystonePage.getX(), this.y + waystonePage.getY(), u, v, waystonePage.getWidth(), waystonePage.getHeight());
            for (Button button : buttons) {
                u = button.getU();
                v = button.getV();
                if (button.isInBounds(mouseX - this.x, mouseY - this.y)) {
                    v += button.getHeight() * (this.mousePressed ? 1 : 2);
                }
                this.drawTexture(matrices, this.x + button.getX(), this.y + button.getY(), u, v, button.getWidth(), button.getHeight());
            }
        }
    }

    private void rename() {
        String name = this.nameField.getText();
        String hash = ((WaystoneScreenHandler)handler).getWaystone();

        if (name == null) {
            name = "";
        }
        ((WaystoneScreenHandler)handler).setName(name);

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        CompoundTag tag = new CompoundTag();
        tag.putString("waystone_name", name);
        tag.putString("waystone_hash", hash);
        data.writeCompoundTag(tag);

        ClientPlayNetworking.send(Utils.ID("rename_waystone"), data);
    }

}