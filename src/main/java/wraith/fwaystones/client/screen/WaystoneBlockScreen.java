package wraith.fwaystones.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.core.Named;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.networking.packets.c2s.RenameWaystone;
import wraith.fwaystones.networking.packets.c2s.RevokeWaystoneOwner;
import wraith.fwaystones.util.Utils;

public class WaystoneBlockScreen extends UniversalWaystoneScreen<WaystoneBlockScreenHandler> {

    private static final Identifier TEXTURE = FabricWaystones.id("textures/gui/stone_waystone.png");
    private static final Identifier CONFIG_TEXTURE = FabricWaystones.id("textures/gui/waystone_config.png");
    public Page page = Page.WAYSTONES;
    private TextFieldWidget nameField;
    private final Button configPage = new Button(154, 5, 18, 18, 207, 0) {
        @Override
        public void onClick() {
            if (!isVisible()) {
                return;
            }
            page = Page.CONFIG;
            texture = CONFIG_TEXTURE;
            handler.updateWaystones(player);
            nameField.setFocused(WaystonePlayerData.getData(player).autofocusWaystoneFields());
            setupButtons();
        }

        @Override
        public boolean isVisible() {
            return page == Page.WAYSTONES;
        }

        @Override
        public void setup() {
            this.tooltip = Text.translatable("fwaystones.config.tooltip.config");
        }
    };

    public WaystoneBlockScreen(WaystoneBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        texture = TEXTURE;

        buttons.add(configPage);

        buttons.add(new Button(154, 5, 18, 18, 177, 0) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                page = Page.WAYSTONES;
                texture = TEXTURE;
                handler.updateWaystones(player);
            }

            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = Text.translatable("fwaystones.config.tooltip.back");
            }
        });

        //Reset name
        buttons.add(new Button(8, backgroundHeight - 32, 13, 13, 177, 54) {
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
                this.tooltip = Text.translatable("fwaystones.config.tooltip.delete_name");
            }
        });

        //Set name
        buttons.add(new ToggleableButton(128, backgroundHeight - 32, 13, 13, 190, 54, 203, 54) {
            @Override
            public void setup() {
                this.tooltip = Text.translatable("fwaystones.config.tooltip.set_name");
                boolean settable = !handler.getName().equals(nameField.getText());
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
                boolean settable = !handler.getName().equals(nameField.getText());
                if (toggled == settable) {
                    toggle();
                }
            }

        });

        //Randomize name
        buttons.add(new Button(143, backgroundHeight - 32, 13, 13, 216, 54) {
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
                this.tooltip = Text.translatable("fwaystones.config.tooltip.randomize_name");
            }
        });

        //Global Toggle
        buttons.add(new ToggleableButton(8, 64, 16, 16, 211, 0, 195, 0) {

            @Override
            public void setup() {
                this.toggled = handler.isGlobal();
                setupTooltip();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                handler.toggleGlobal();

                setupTooltip();
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            private void setupTooltip() {
                this.tooltip = this.toggled
                    ? Text.translatable("fwaystones.config.tooltip.make_non_global")
                    : Text.translatable("fwaystones.config.tooltip.make_global");
            }
        });

        // View discovered
        buttons.add(new ToggleableButton(8, 26, 13, 13, 177, 54, 190, 54) {
            @Override
            public void setup() {
                this.toggled = WaystonePlayerData.getData(player).viewDiscoveredWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                WaystonePlayerData.getData(player).toggleViewDiscoveredWaystones();
                handler.updateWaystones(player);
            }

            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }

        });

        //View global
        buttons.add(new ToggleableButton(8, 42, 13, 13, 177, 54, 190, 54) {
            @Override
            public void setup() {
                this.toggled = WaystonePlayerData.getData(player).viewGlobalWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                WaystonePlayerData.getData(player).toggleViewGlobalWaystones();
                handler.updateWaystones(player);
            }

            @Override
            public boolean isVisible() {
                return page == Page.CONFIG;
            }
        });

        //Revoke ownership
        buttons.add(new Button(33, 66, 11, 13, 227, 0) {
            @Override
            public void onClick() {
                super.onClick();
                var uuid = handler.uuid();
                WaystoneNetworkHandler.CHANNEL.clientHandle().send(new RevokeWaystoneOwner(uuid));
                handler.removeOwner();
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG && handler.hasOwner();
            }

            @Override
            public void setup() {
                this.tooltip = Text.translatable("fwaystones.config.tooltip.revoke_ownership");
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new TextFieldWidget(this.textRenderer, this.x + 28, this.y + backgroundHeight - 30, 93, 10, Text.literal("")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
                if (bl && button == 1) {
                    this.setText("");
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void setFocused(boolean lookForwards) {
                if (isVisible()) {
                    super.setFocused(lookForwards);
                }
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return isVisible() && mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
            }
        };
        this.nameField.setMaxLength(16);
        this.nameField.setEditableColor(0xFFFFFF);
        this.nameField.setDrawsBackground(false);
        this.nameField.setFocusUnlocked(true);
        var data = WaystoneDataStorage.getStorage(this.player).getData(handler.uuid());
        this.nameField.setText((data instanceof Named named) ? named.name() : "");
        this.nameField.setChangedListener((s) -> {
            boolean settable = !handler.getName().equals(s);
            // TODO: unhardcode this
            ToggleableButton button = ((ToggleableButton) buttons.get(5));
            if (button.isToggled() == settable) {
                button.toggle();
            }
        });
        this.addDrawableChild(this.nameField);
    }

    @Override
    protected boolean searchVisible() {
        return page == Page.WAYSTONES;
    }

    private boolean canEdit() {
        return handler.isOwner(player) || player.hasPermissionLevel(2);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.nameField != null && this.nameField.isVisible()) {
            if (WaystonePlayerData.getData(player).autofocusWaystoneFields()) {
                this.nameField.setFocused(true);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
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
            if (InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent() && this.handleHotbarKeyPressed(keyCode, scanCode)) {
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
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            super.drawBackground(context, delta, mouseX, mouseY);
        } else {
            context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawTexture(CONFIG_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            if (canEdit()) {
                context.drawTexture(CONFIG_TEXTURE, x + 23, y + backgroundHeight - 33, 0, backgroundHeight, 103, 15);
            }
            renderButtons(context, mouseX, mouseY);
            renderButtonText(context);
            String owner = handler.getOwnerName();
            if (owner == null || "".equals(owner)) {
                owner = Text.translatable("fwaystones.config.no_owner").getString();
            }
            context.drawText(textRenderer, Text.translatable("fwaystones.config.owner", owner), this.x + 10, this.y + 10, 0x161616, false);
            if (this.nameField.isVisible()) {
                this.nameField.render(context, mouseX, mouseY, delta);
            }
            renderButtonTooltips(context, mouseX, mouseY);
        }
    }

    @Override
    protected void renderWaystoneBackground(DrawContext context, int mouseX, int mouseY, int x, int y, int m) {
        for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.backgroundHeight;
            if (handler.position().equals(getDiscoveredWaystones().get(n))) {
                s += 18;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            context.drawTexture(TEXTURE, x, r - 1, 0, s, 101, 18);
        }
    }

    private void renderButtonText(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("fwaystones.config.view_discovered"), this.x + 25, this.y + 29, 0x161616, false);
        context.drawText(textRenderer, Text.translatable("fwaystones.config.view_global"), this.x + 25, this.y + 45, 0x161616, false);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            context.drawText(textRenderer, Utils.formatWaystoneName(handler.getName()), this.titleX, this.titleY, 4210752, false);
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
        if (page == Page.WAYSTONES && configPage.isVisible() && configPage.isInBounds((int) mouseX - this.x, (int) mouseY - this.y)) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            configPage.onClick();
            return super.superMouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean canClickWaystones() {
        return page == Page.WAYSTONES;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (page == Page.WAYSTONES) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        } else {
            return super.superMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
    }

    private void rename() {
        if (!canEdit()) return;

        var name = this.nameField.getText();

        if (name == null) name = "";

        handler.setName(name);

        WaystoneNetworkHandler.CHANNEL.clientHandle().send(new RenameWaystone(handler.uuid(), name));
    }

    protected enum Page {
        WAYSTONES,
        CONFIG
    }

}
