package wraith.fwaystones.screen;

import net.minecraft.server.permissions.Permissions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.packets.RemoveWaystoneOwnerPacket;
import wraith.fwaystones.packets.RenameWaystonePacket;
import wraith.fwaystones.packets.SyncPlayerFromClientPacket;
import wraith.fwaystones.util.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.UUID;

public class WaystoneBlockScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/waystone.png");
    private static final Identifier CONFIG_TEXTURE = Utils.ID("textures/gui/waystone_config.png");
    public Page page = Page.WAYSTONES;
    private EditBox nameField;
    private final Button configPage = new Button(154, 5, 18, 18, 207, 0) {
        @Override
        public void onClick() {
            if (!isVisible()) {
                return;
            }
            page = Page.CONFIG;
            texture = CONFIG_TEXTURE;
            ((UniversalWaystoneScreenHandler) menu).updateWaystones(inventory.player);
            nameField.setFocused(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$autofocusWaystoneFields());
            setupButtons();
        }

        @Override
        public boolean isVisible() {
            return page == Page.WAYSTONES;
        }

        @Override
        public void setup() {
            this.tooltip = Component.translatable("fwaystones.config.tooltip.config");
        }
    };

    public WaystoneBlockScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
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
                this.tooltip = Component.translatable("fwaystones.config.tooltip.back");
            }
        });

        //Reset name
        buttons.add(new Button(8, imageHeight - 32, 13, 13, 177, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setValue("");
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = Component.translatable("fwaystones.config.tooltip.delete_name");
            }
        });

        //Set name
        buttons.add(new ToggleableButton(128, imageHeight - 32, 13, 13, 190, 54, 203, 54) {
            @Override
            public void setup() {
                this.tooltip = Component.translatable("fwaystones.config.tooltip.set_name");
                boolean settable = !((WaystoneBlockScreenHandler) handler).getName().equals(nameField.getValue());
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
                boolean settable = !((WaystoneBlockScreenHandler) handler).getName().equals(nameField.getValue());
                if (toggled == settable) {
                    toggle();
                }
            }

        });

        //Randomize name
        buttons.add(new Button(143, imageHeight - 32, 13, 13, 216, 54) {
            @Override
            public void onClick() {
                if (!isVisible()) {
                    return;
                }
                super.onClick();
                nameField.setValue(Utils.generateWaystoneName(""));
            }

            @Override
            public boolean isVisible() {
                return canEdit() && page == Page.CONFIG;
            }

            @Override
            public void setup() {
                this.tooltip = Component.translatable("fwaystones.config.tooltip.randomize_name");
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
                    ? Component.translatable("fwaystones.config.tooltip.make_non_global")
                    : Component.translatable("fwaystones.config.tooltip.make_global");
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
                ClientPlayNetworking.send(new SyncPlayerFromClientPacket(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toTagW(new CompoundTag())));
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
                ClientPlayNetworking.send(new SyncPlayerFromClientPacket(((PlayerEntityMixinAccess) inventory.player).fabricWaystones$toTagW(new CompoundTag())));
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
                this.tooltip = Component.translatable("fwaystones.config.tooltip.revoke_ownership");
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new EditBox(this.font, this.leftPos + 28, this.topPos + imageHeight - 30, 93, 10, Component.literal("")) {
            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
                boolean bl = event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width) && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height);
                if (bl && event.button() == 1) {
                    this.setValue("");
                }
                return super.mouseClicked(event, doubled);
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
        this.nameField.setTextColor(0xFFFFFFFF);
        this.nameField.setBordered(false);
        this.nameField.setCanLoseFocus(true);
        String waystone = FabricWaystones.WAYSTONE_STORAGE.getName(((WaystoneBlockScreenHandler) menu).getWaystone());
        this.nameField.setValue(waystone == null ? "" : waystone);
        this.nameField.setResponder((s) -> {
            boolean settable = !((WaystoneBlockScreenHandler) menu).getName().equals(s);
            // TODO: unhardcode this
            ToggleableButton button = ((ToggleableButton) buttons.get(5));
            if (button.isToggled() == settable) {
                button.toggle();
            }
        });
        this.addRenderableWidget(this.nameField);
    }

    @Override
    protected boolean searchVisible() {
        return page == Page.WAYSTONES;
    }

    private boolean canEdit() {
        return ((WaystoneBlockScreenHandler) menu).isOwner(inventory.player) || inventory.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.nameField != null && this.nameField.isVisible()) {
//            this.nameField.tick();
            if (((PlayerEntityMixinAccess) minecraft.player).fabricWaystones$autofocusWaystoneFields()) {
                this.nameField.setFocused(true);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (page == Page.WAYSTONES) {
            super.resize(width, height);
        } else {
            String string = this.nameField.getValue();
            this.init(width, height);
            this.nameField.setValue(string);
        }
        super.superResize(width, height);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (page == Page.WAYSTONES) {
            return super.charTyped(event);
        } else {
            if (this.ignoreTypedCharacter) {
                return false;
            } else {
                return this.nameField.isVisible() && this.nameField.charTyped(event);
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (page == Page.WAYSTONES) {
            return super.keyPressed(event);
        } else {
            this.ignoreTypedCharacter = false;
            if (InputConstants.getKey(event).getNumericKeyValue().isPresent() && this.checkHotbarKeyPressed(event)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                if (this.nameField.isVisible() && this.nameField.keyPressed(event)) {
                    return true;
                } else {
                    return this.nameField.isVisible() && this.nameField.isFocused() && this.nameField.isVisible() && event.key() != InputConstants.KEY_ESCAPE || super.keyPressed(event);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (page == Page.WAYSTONES) {
            super.extractBackground(context, mouseX, mouseY, delta);
        } else {
            superExtractBackground(context, mouseX, mouseY, delta);
            int color = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
            context.blit(RenderPipelines.GUI_TEXTURED, CONFIG_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256, color);
            if (canEdit()) {
                context.blit(RenderPipelines.GUI_TEXTURED, CONFIG_TEXTURE, leftPos + 23, topPos + imageHeight - 33, 0, imageHeight, 103, 15, 256, 256, color);
            }
            renderButtons(context, mouseX, mouseY);
            renderButtonText(context);
            String owner = ((WaystoneBlockScreenHandler) menu).getOwnerName();
            if (owner == null || "".equals(owner)) {
                owner = Component.translatable("fwaystones.config.no_owner").getString();
            }
            context.text(font, Component.translatable("fwaystones.config.owner", owner), this.leftPos + 10, this.topPos + 10, 0xFF161616, false);
            if (this.nameField.isVisible()) {
                this.nameField.extractWidgetRenderState(context, mouseX, mouseY, delta);
            }
            renderButtonTooltips(context, mouseX, mouseY);
        }
    }

    @Override
    protected void renderWaystoneBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, int x, int y, int m) {
        for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
            int o = n - this.scrollOffset;
            int r = y + o * 18 + 2;
            int s = this.imageHeight;
            if (((WaystoneBlockScreenHandler) menu).getWaystone().equals(getDiscoveredWaystones().get(n))) {
                s += 18;
            } else if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, r - 1, 0, s, 101, 18, 256, 256);
        }
    }

    private void renderButtonText(GuiGraphicsExtractor context) {
        context.text(font, Component.translatable("fwaystones.config.view_discovered"), this.leftPos + 25, this.topPos + 29, 0xFF161616, false);
        context.text(font, Component.translatable("fwaystones.config.view_global"), this.leftPos + 25, this.topPos + 45, 0xFF161616, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (page == Page.WAYSTONES) {
            context.text(font, ((WaystoneBlockScreenHandler) menu).getName(), this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        }
    }

    @Override
    protected void slotClicked(Slot slot, int invSlot, int clickData, ContainerInput actionType) {
        if (page == Page.WAYSTONES) {
            super.slotClicked(slot, invSlot, clickData, actionType);
        } else {
            super.superOnMouseClick(slot, invSlot, clickData, actionType);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (page == Page.WAYSTONES && configPage.isVisible() && configPage.isInBounds((int) event.x() - this.leftPos, (int) event.y() - this.topPos)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            configPage.onClick();
            return super.superMouseClicked(event, doubled);
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    protected boolean canClickWaystones() {
        return page == Page.WAYSTONES;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (page == Page.WAYSTONES) {
            return super.mouseDragged(event, deltaX, deltaY);
        } else {
            return super.superMouseDragged(event, deltaX, deltaY);
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
        String name = this.nameField.getValue();
        String hash = ((WaystoneBlockScreenHandler) menu).getWaystone();
        UUID owner = ((WaystoneBlockScreenHandler) menu).getOwner();

        if (name == null) {
            name = "";
        }
        ((WaystoneBlockScreenHandler) menu).setName(name);

        ClientPlayNetworking.send(new RenameWaystonePacket(owner, hash, name));
    }

    protected enum Page {
        WAYSTONES,
        CONFIG
    }

}