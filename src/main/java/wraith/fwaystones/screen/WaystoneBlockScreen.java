package wraith.fwaystones.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.packets.RemoveWaystoneOwnerPacket;
import wraith.fwaystones.packets.RenameWaystonePacket;
import wraith.fwaystones.packets.SyncPlayerFromClientPacket;
import wraith.fwaystones.util.Utils;
import java.util.UUID;

public class WaystoneBlockScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/waystone.png");
    private static final Identifier CONFIG_TEXTURE = Utils.ID("textures/gui/waystone_config.png");
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
            ((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
            nameField.setFocused(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$autofocusWaystoneFields());
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

    public WaystoneBlockScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
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
                ((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
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
                boolean settable = !((WaystoneBlockScreenHandler) handler).getName().equals(nameField.getText());
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
                boolean settable = !((WaystoneBlockScreenHandler) handler).getName().equals(nameField.getText());
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
                this.toggled = ((WaystoneBlockScreenHandler) handler).isGlobal();
                setupTooltip();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((WaystoneBlockScreenHandler) handler).toggleGlobal();

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
                this.toggled = ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$shouldViewDiscoveredWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toggleViewDiscoveredWaystones();
                ((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
                ClientPlayNetworking.send(new SyncPlayerFromClientPacket(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toTagW(new NbtCompound())));
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
                this.toggled = ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$shouldViewGlobalWaystones();
            }

            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                ((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toggleViewGlobalWaystones();
                ((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
                ClientPlayNetworking.send(new SyncPlayerFromClientPacket(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toTagW(new NbtCompound())));
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
                String hash = ((WaystoneBlockScreenHandler) handler).getWaystone();
                UUID owner = ((WaystoneBlockScreenHandler) handler).getOwner();
                ClientPlayNetworking.send(new RemoveWaystoneOwnerPacket(owner, hash));
                ((WaystoneBlockScreenHandler) handler).removeOwner();
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG && ((WaystoneBlockScreenHandler) handler).hasOwner();
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
        String waystone = FabricWaystones.WAYSTONE_STORAGE.getName(((WaystoneBlockScreenHandler) handler).getWaystone());
        this.nameField.setText(waystone == null ? "" : waystone);
        this.nameField.setChangedListener((s) -> {
            boolean settable = !((WaystoneBlockScreenHandler) handler).getName().equals(s);
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
        return ((WaystoneBlockScreenHandler) handler).isOwner(inventory.player) || inventory.player.hasPermissionLevel(2);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.nameField != null && this.nameField.isVisible()) {
//            this.nameField.tick();
            if (((PlayerEntityMixinAccess) client.player).fabricWaystones$autofocusWaystoneFields()) {
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
            int color = ColorHelper.fromFloats(1.0F, 1.0F, 1.0F, 1.0F);
            context.drawTexture(RenderLayer::getGuiTextured, CONFIG_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256, color);
            if (canEdit()) {
                context.drawTexture(RenderLayer::getGuiTextured, CONFIG_TEXTURE, x + 23, y + backgroundHeight - 33, 0, backgroundHeight, 103, 15, 256, 256, color);
            }
            renderButtons(context, mouseX, mouseY);
            renderButtonText(context);
            String owner = ((WaystoneBlockScreenHandler) handler).getOwnerName();
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
            if (((WaystoneBlockScreenHandler) handler).getWaystone().equals(getDiscoveredWaystones().get(n))) {
                s += 18;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, r - 1, 0, s, 101, 18, 256, 256);
        }
    }

    private void renderButtonText(DrawContext context) {
        context.drawText(textRenderer, Text.translatable("fwaystones.config.view_discovered"), this.x + 25, this.y + 29, 0x161616, false);
        context.drawText(textRenderer, Text.translatable("fwaystones.config.view_global"), this.x + 25, this.y + 45, 0x161616, false);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            context.drawText(textRenderer, ((WaystoneBlockScreenHandler) handler).getName(), this.titleX, this.titleY, 4210752, false);
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
        if (!canEdit()) {
            return;
        }
        String name = this.nameField.getText();
        String hash = ((WaystoneBlockScreenHandler) handler).getWaystone();
        UUID owner = ((WaystoneBlockScreenHandler) handler).getOwner();

        if (name == null) {
            name = "";
        }
        ((WaystoneBlockScreenHandler) handler).setName(name);

        ClientPlayNetworking.send(new RenameWaystonePacket(owner, hash, name));
    }

    protected enum Page {
        WAYSTONES,
        CONFIG
    }

}