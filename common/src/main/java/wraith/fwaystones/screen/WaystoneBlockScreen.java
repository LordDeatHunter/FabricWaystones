package wraith.fwaystones.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.Utils;

import java.util.UUID;

public class WaystoneBlockScreen extends UniversalWaystoneScreen {

	private static final ResourceLocation TEXTURE = Utils.ID("textures/gui/waystone.png");
	private static final ResourceLocation CONFIG_TEXTURE = Utils.ID("textures/gui/waystone_config.png");
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
			nameField.setFocused(((PlayerEntityMixinAccess) inventory.player).autofocusWaystoneFields());
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
				this.toggled = ((PlayerEntityMixinAccess) inventory.player).shouldViewDiscoveredWaystones();
			}

			@Override
			public void onClick() {
				if (!isVisible()) {
					return;
				}
				super.onClick();
				((PlayerEntityMixinAccess) inventory.player).toggleViewDiscoveredWaystones();
				((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
				FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
				packet.writeNbt(((PlayerEntityMixinAccess) inventory.player).toTagW(new CompoundTag()));
				NetworkManager.sendToServer(PacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
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
				this.toggled = ((PlayerEntityMixinAccess) inventory.player).shouldViewGlobalWaystones();
			}

			@Override
			public void onClick() {
				if (!isVisible()) {
					return;
				}
				super.onClick();
				((PlayerEntityMixinAccess) inventory.player).toggleViewGlobalWaystones();
				((UniversalWaystoneScreenHandler) handler).updateWaystones(inventory.player);
				FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
				packet.writeNbt(((PlayerEntityMixinAccess) inventory.player).toTagW(new CompoundTag()));
				NetworkManager.sendToServer(PacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
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
				FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
				CompoundTag tag = new CompoundTag();
				tag.putString("waystone_hash", ((WaystoneBlockScreenHandler) handler).getWaystone());
				UUID owner = ((WaystoneBlockScreenHandler) handler).getOwner();
				tag.putUUID("waystone_owner", owner);
				packet.writeNbt(tag);
				NetworkManager.sendToServer(PacketHandler.REMOVE_WAYSTONE_OWNER, packet);
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
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				boolean bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);
				if (bl && button == 1) {
					this.setValue("");
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
		this.nameField.setTextColor(0xFFFFFF);
		this.nameField.setBordered(false);
		this.nameField.setCanLoseFocus(true);
		String waystone = Waystones.STORAGE.getName(((WaystoneBlockScreenHandler) menu).getWaystone());
		this.nameField.setValue(waystone == null ? "" : waystone);
		this.nameField.setResponder((s) -> {
			boolean settable = !((WaystoneBlockScreenHandler) menu).getName().equals(s);
			// TODO: unhardcode this
			ToggleableButton button = ((ToggleableButton) buttons.get(5));
			if (button.isToggled() == settable) {
				button.toggle();
			}
		});
		this.addWidget(this.nameField);
	}

	@Override
	protected boolean searchVisible() {
		return page == Page.WAYSTONES;
	}

	private boolean canEdit() {
		return ((WaystoneBlockScreenHandler) menu).isOwner(inventory.player) || inventory.player.hasPermissions(2);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		if (this.nameField != null && this.nameField.isVisible()) {
			this.nameField.tick();
			if (((PlayerEntityMixinAccess) minecraft.player).autofocusWaystoneFields()) {
				this.nameField.setFocused(true);
			}
		}
	}

	@Override
	public void render(PoseStack context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.renderTooltip(context, mouseX, mouseY);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		if (page == Page.WAYSTONES) {
			super.resize(client, width, height);
		} else {
			String string = this.nameField.getValue();
			this.init(client, width, height);
			this.nameField.setValue(string);
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
			if (InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent() && this.checkHotbarKeyPressed(keyCode, scanCode)) {
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
	protected void renderBg(PoseStack context, float delta, int mouseX, int mouseY) {
		if (page == Page.WAYSTONES) {
			super.renderBg(context, delta, mouseX, mouseY);
		} else {
			this.renderBackground(context);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, CONFIG_TEXTURE);
			this.blit(context, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
			if (canEdit()) {
				this.blit(context, leftPos + 23, topPos + imageWidth - 33, 0, imageHeight, 103, 15);
			}
			renderButtons(context, mouseX, mouseY);
			renderButtonText(context);
			String owner = ((WaystoneBlockScreenHandler) menu).getOwnerName();
			if (owner == null || "".equals(owner)) {
				owner = Component.translatable("fwaystones.config.no_owner").getString();
			}
			this.font.draw(context, Component.translatable("fwaystones.config.owner", owner), this.leftPos + 10, this.topPos + 10, 0x161616);
			if (this.nameField.isVisible()) {
				this.nameField.render(context, mouseX, mouseY, delta);
			}
			renderButtonTooltips(context, mouseX, mouseY);
		}
	}

	@Override
	protected void renderWaystoneBackground(PoseStack context, int mouseX, int mouseY, int x, int y, int m) {
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
			this.blit(context, x, r - 1, 0, s, 101, 18);
		}
	}

	private void renderButtonText(PoseStack context) {
		this.font.draw(context, Component.translatable("fwaystones.config.view_discovered"), this.leftPos + 25, this.topPos + 29, 0x161616);
		this.font.draw(context, Component.translatable("fwaystones.config.view_global"), this.leftPos + 25, this.topPos + 45, 0x161616);
	}

	@Override
	protected void renderLabels(PoseStack context, int mouseX, int mouseY) {
		if (page == Page.WAYSTONES) {
			this.font.draw(context, ((WaystoneBlockScreenHandler) menu).getName(), this.titleLabelX, this.titleLabelY, 4210752);
		}
	}

	@Override
	protected void slotClicked(Slot slot, int invSlot, int clickData, ClickType actionType) {
		if (page == Page.WAYSTONES) {
			super.slotClicked(slot, invSlot, clickData, actionType);
		} else {
			super.superOnMouseClick(slot, invSlot, clickData, actionType);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (page == Page.WAYSTONES && configPage.isVisible() && configPage.isInBounds((int) mouseX - this.leftPos, (int) mouseY - this.topPos)) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (page == Page.WAYSTONES) {
			return super.mouseScrolled(mouseX, mouseY, amount);
		} else {
			return super.superMouseScrolled(mouseX, mouseY, amount);
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

		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

		CompoundTag tag = new CompoundTag();
		tag.putString("waystone_name", name);
		tag.putString("waystone_hash", hash);
		if (owner != null) {
			tag.putUUID("waystone_owner", owner);
		}
		data.writeNbt(tag);

		NetworkManager.sendToServer(PacketHandler.RENAME_WAYSTONE, data);
	}

	protected enum Page {
		WAYSTONES,
		CONFIG
	}

}
