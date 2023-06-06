package wraith.fwaystones.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.util.PacketHandler;
import wraith.fwaystones.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class UniversalScreen extends AbstractContainerScreen<AbstractContainerMenu> {
	protected final Inventory inventory;
	protected final ArrayList<Button> buttons = new ArrayList<>();
	private final ResourceLocation texture;
	protected float scrollAmount;
	protected boolean mouseClicked;
	protected int scrollOffset;
	protected boolean ignoreTypedCharacter;
	protected boolean mousePressed;
	private EditBox searchField;

	public UniversalScreen(AbstractContainerMenu handler, Inventory inventory, ResourceLocation texture, Component title) {
		super(handler, inventory, title);
		this.inventory = inventory;
		this.texture = texture;
		this.imageWidth = 177;
		this.imageHeight = 176;
		buttons.add(new Button(140, 25, 13, 13, 225, 0) {
			@Override
			public void onClick() {
				if (!isVisible()) {
					return;
				}
				super.onClick();
				((Universalmenu) handler).toggleSearchType();
				searchField.changeFocus(((PlayerEntityMixinAccess) minecraft.player).autofocusWaystoneFields());
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
			public Component tooltip() {
				return ((Universalmenu) handler).getSearchTypeTooltip();
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
				return !(UniversalScreen.this instanceof WaystoneScreen waystoneBlockScreen) || waystoneBlockScreen.page == WaystoneScreen.Page.WAYSTONES;
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
						? Component.translatable("fwaystones.config.tooltip.unlock_search")
						: Component.translatable("fwaystones.config.tooltip.lock_search");
			}
		});

		setupButtons();
	}

	@Override
	public void onClose() {
		super.onClose();
		FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
		packet.writeNbt(((PlayerEntityMixinAccess) inventory.player).toTagW(new CompoundTag()));
		NetworkManager.sendToServer(PacketHandler.SYNC_PLAYER_FROM_CLIENT, packet);
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

		this.searchField = new EditBox(this.font, this.leftPos + 37, this.topPos + 27, 93, 10, Component.literal("")) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				boolean bl = mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
				if (bl && button == 1) {
					this.setValue("");
				}
				return super.mouseClicked(mouseX, mouseY, button);
			}
		};
		this.searchField.setMaxLength(16);
		this.searchField.setTextColor(0xFFFFFF);
		this.searchField.setVisible(true);
		this.searchField.setBordered(false);
		this.searchField.setCanLoseFocus(true);
		this.searchField.setValue("");
		this.searchField.setResponder((s) -> {
			this.scrollAmount = 0;
			this.scrollOffset = (int) ((double) (this.scrollAmount * (float) this.getMaxScroll()) + 0.5D);
			((Universalmenu) menu).setFilter(this.searchField != null ? this.searchField.getValue() : "");
			((Universalmenu) menu).filterWaystones();
		});
		this.addWidget(this.searchField);
	}

	@Override
	public void containerTick() {
		if (this.searchField != null && this.searchField.isVisible()) {
			this.searchField.tick();
			if (((PlayerEntityMixinAccess) minecraft.player).autofocusWaystoneFields()) {
				this.searchField.setFocus(true);
			}
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.searchField.getValue();
		this.init(client, width, height);
		this.searchField.setValue(string);
		super.resize(client, width, height);
	}

	@Override
	protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
		this.renderBackground(matrices);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		this.blit(matrices, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
		int k = (int) (75.0F * this.scrollAmount);
		this.blit(matrices, leftPos + 141, topPos + 40 + k, 177 + (this.shouldScroll() ? 0 : 11), 0, 11, 15);
		int n = this.scrollOffset + 5;
		// TODO: Merge some of these
		this.renderWaystoneBackground(matrices, mouseX, mouseY, this.leftPos + 36, this.topPos + 39, n);
		this.renderForgetButtons(matrices, mouseX, mouseY, this.leftPos + 24, this.topPos + 45);
		renderButtons(matrices, mouseX, mouseY);
		this.renderCostItem(matrices, this.leftPos + 40, this.topPos + 136);
		this.renderWaystoneNames(matrices, this.leftPos + 36, this.topPos + 40, n);
		this.renderWaystoneTooltips(matrices, mouseX, mouseY, this.leftPos + 36, this.topPos + 39, n);
		this.renderWaystoneAmount(matrices, this.leftPos + 10, this.topPos + 160);
		this.searchField.render(matrices, mouseX, mouseY, delta);
		this.renderForgetTooltips(matrices, mouseX, mouseY, this.leftPos + 24, this.topPos + 45);
		this.renderButtonTooltips(matrices, mouseX, mouseY);
	}

	protected void renderButtonTooltips(PoseStack matrices, int mouseX, int mouseY) {
		for (Button button : buttons) {
			if (!button.isVisible() || !button.hasToolTip() || !button.isInBounds(mouseX - this.leftPos, mouseY - this.topPos)) {
				continue;
			}
			this.renderTooltip(matrices, button.tooltip(), mouseX, mouseY);
		}
	}

	private void renderWaystoneAmount(PoseStack matrices, int x, int y) {
		this.font.draw(matrices, Component.translatable("fwaystones.gui.displayed_waystones", this.getDiscoveredCount()), x, y, 0x161616);
	}

	protected void renderButtons(PoseStack matrices, int mouseX, int mouseY) {
		for (Button button : buttons) {
			if (!button.isVisible()) {
				continue;
			}
			int u = button.getU();
			int v = button.getV();
			if (button.isInBounds(mouseX - this.leftPos, mouseY - this.topPos)) {
				v += button.getHeight() * (this.mousePressed ? 1 : 2);
			}
			this.blit(matrices, this.leftPos + button.getX(), this.topPos + button.getY(), u, v, button.getWidth(), button.getHeight());
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
		if (InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent() && this.checkHotbarKeyPressed(keyCode, scanCode)) {
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
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.renderTooltip(matrices, mouseX, mouseY);
	}

	protected void renderCostItem(PoseStack matrices, int x, int y) {
		var config = Waystones.CONFIG.teleportation_cost;

		MutableComponent text;
		switch (config.cost_type) {
			case HEALTH -> {
				this.blit(matrices, x + 4, y + 4, 186, 15, 9, 9);
				text = Component.translatable("fwaystones.cost.health");
			}
			case HUNGER -> {
				this.blit(matrices, x + 4, y + 4, 177, 24, 9, 9);
				text = Component.translatable("fwaystones.cost.hunger");
			}
			case EXPERIENCE -> {
				this.blit(matrices, x + 4, y + 4, 177, 15, 9, 9);
				text = Component.translatable("fwaystones.cost.xp");
			}
			case LEVEL -> {
				this.itemRenderer.renderGuiItem(new ItemStack(Items.EXPERIENCE_BOTTLE), x, y);
				text = Component.translatable("fwaystones.cost.level");
			}
			case ITEM -> {
				var item = Registry.ITEM.get(Utils.getTeleportCostItem());
				this.itemRenderer.renderGuiItem(new ItemStack(item), x, y);
				text = (MutableComponent) item.getDescription();
			}
			default -> text = Component.translatable("fwaystones.cost.free");
		}

		renderCostText(matrices, x, y, text);
	}

	protected void renderCostText(PoseStack matrices, int x, int y, MutableComponent text) {
		renderCostText(matrices, x, y, text, 0x161616);
	}

	protected void renderCostText(PoseStack matrices, int x, int y, MutableComponent text, int color) {
		this.font.draw(matrices, text.append(Component.literal(": " + Waystones.CONFIG.teleportation_cost.base_cost)), x + 20, y + 5, color);
	}

	@Override
	protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
		this.font.draw(matrices, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
	}


	protected void renderForgetButtons(PoseStack matrixStack, int mouseX, int mouseY, int x, int y) {
		int n = getDiscoveredCount();
		for (int i = 0; i < 5; ++i) {
			int r = y + i * 18;
			int v = 0;
			if (i >= n) {
				v = 8;
			} else if (mouseX >= x && mouseY >= r && mouseX < x + 8 && mouseY < r + 8) {
				v += 8 * (mouseClicked ? 1 : 2);
			}
			this.blit(matrixStack, x, r, 199, v, 8, 8);
		}
	}

	protected void renderForgetTooltips(PoseStack matrixStack, int mouseX, int mouseY, int x, int y) {
		int n = getDiscoveredCount();
		for (int i = 0; i < n; ++i) {
			int r = y + i * 18;
			if (mouseX < x || mouseY < r || mouseX > x + 8 || mouseY >= r + 8) {
				continue;
			}
			this.renderTooltip(matrixStack, Component.translatable("fwaystones.gui.forget_tooltip"), mouseX, mouseY);
		}
	}

	protected void renderWaystoneBackground(PoseStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {
		for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
			int o = n - this.scrollOffset;
			int r = y + o * 18 + 2;
			int s = this.imageHeight;
			if (mouseX >= x && mouseY >= r && mouseX < x + 101 && mouseY < r + 18) {
				s += mouseClicked ? 18 : 36;
			}
			this.blit(matrixStack, x, r - 1, 0, s, 101, 18);
		}
	}

	protected void renderWaystoneTooltips(PoseStack matrixStack, int mouseX, int mouseY, int x, int y, int m) {

		ArrayList<String> waystones = getDiscoveredWaystones();
		for (int n = this.scrollOffset; n < m && n < getDiscoveredCount(); ++n) {
			int o = n - this.scrollOffset;
			int r = y + o * 18 + 2;
			if (mouseX < x || mouseY < r || mouseX >= x + 101 || mouseY >= r + 18) {
				continue;
			}
			var waystoneData = Waystones.WAYSTONE_STORAGE.getWaystoneData(waystones.get(n));
			if (waystoneData == null) {
				continue;
			}
			var startDim = Utils.getDimensionName(minecraft.player.level);
			var endDim = waystoneData.getWorldName();
			List<Component> tooltipContents = new ArrayList<>();
			tooltipContents.add(Component.translatable("fwaystones.gui.cost_tooltip", Utils.getCost(Vec3.atCenterOf(waystoneData.way_getPos()), minecraft.player.position(), startDim, endDim)));
			if (hasShiftDown()) {
				tooltipContents.add(Component.translatable("fwaystones.gui.dimension_tooltip", waystoneData.getWorldName()));
			}
			this.renderComponentTooltip(matrixStack, tooltipContents, mouseX, mouseY);
		}

	}

	protected void renderWaystoneNames(PoseStack matrices, int x, int y, int m) {
		if (Waystones.WAYSTONE_STORAGE == null)
			return;
		ArrayList<String> waystones = getDiscoveredWaystones();
		for (int n = this.scrollOffset; n < m && n < waystones.size(); ++n) {
			int o = n - this.scrollOffset;
			int r = y + o * 18 + 2;

			String name = Waystones.WAYSTONE_STORAGE.getName(waystones.get(n));
			this.font.draw(matrices, name, x + 5f, r - 1 + 5f, 0x161616);
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
			if (!guiButton.isVisible() || !guiButton.isInBounds((int) mouseX - this.leftPos, (int) mouseY - this.topPos)) {
				continue;
			}
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			guiButton.onClick();
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	protected boolean canClickWaystones() {
		return true;
	}

	protected boolean tryClick(double mouseX, double mouseY) {
		int forgetButtonX = this.leftPos + 24;
		int forgetButtonY = this.topPos + 45;
		int waystoneButtonX = this.leftPos + 36;
		int waystoneButtonY = this.topPos + 40;
		int adjustedScrollOffset = this.scrollOffset + 5;

		int n = getDiscoveredCount();
		for (int currentWaystone = this.scrollOffset; currentWaystone < adjustedScrollOffset && currentWaystone < n; ++currentWaystone) {
			int currentWaystoneOffsetPosition = currentWaystone - this.scrollOffset;
			int forgetButtonStartX = (int) (mouseX - forgetButtonX);
			int forgetButtonStartY = (int) (mouseY - (forgetButtonY + currentWaystoneOffsetPosition * 18));

			int waystoneButtonStartX = (int) (mouseX - waystoneButtonX);
			int waystoneButtonStartY = (int) (mouseY - (waystoneButtonY + currentWaystoneOffsetPosition * 18));
			if (currentWaystoneOffsetPosition < n && forgetButtonStartX >= 0.0D && forgetButtonStartY >= 0.0D && forgetButtonStartX < 8 && forgetButtonStartY < 8 && (this.menu).clickMenuButton(this.minecraft.player, currentWaystone * 2 + 1)) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_BREAK, 1.0F));
				this.scrollOffset = Math.max(0, this.scrollOffset - 1);

				CompoundTag tag = new CompoundTag();
				tag.putInt("sync_id", menu.containerId);
				tag.putInt("clicked_slot", currentWaystone * 2 + 1);
				FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer()).writeNbt(tag);
				NetworkManager.sendToServer(PacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);
				return true;
			}
			if (menu instanceof WaystoneMenu waystoneBlockScreenHandler && waystoneBlockScreenHandler.getWaystone().equals(getDiscoveredWaystones().get(currentWaystone))) {
				continue;
			}
			if (waystoneButtonStartX >= 0.0D && waystoneButtonStartY >= 0.0D && waystoneButtonStartX < 101.0D && waystoneButtonStartY < 18.0D && (this.menu).clickMenuButton(this.minecraft.player, currentWaystone * 2)) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

				CompoundTag tag = new CompoundTag();
				tag.putInt("sync_id", menu.containerId);
				tag.putInt("clicked_slot", currentWaystone * 2);
				FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer()).writeNbt(tag);
				NetworkManager.sendToServer(PacketHandler.WAYSTONE_GUI_SLOT_CLICK, packet);
				return true;
			}
		}

		int i3 = this.leftPos + 141;
		int j3 = this.topPos + 40;
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
			this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
			this.scrollOffset = (int) ((double) (this.scrollAmount * (float) i) + 0.5D);
		}

		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.mouseClicked && this.shouldScroll()) {
			int i = this.topPos + 40;
			int j = i + 90;
			this.scrollAmount = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
			this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
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
		return ((Universalmenu) menu).getWaystonesCount();
	}

	protected ArrayList<String> getDiscoveredWaystones() {
		return ((Universalmenu) menu).getSearchedWaystones();
	}

	protected boolean superMouseScrolled(double mouseX, double mouseY, double amount) {
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	protected boolean superMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	protected void superResize(Minecraft client, int width, int height) {
		super.resize(client, width, height);
	}

	protected void superOnMouseClick(Slot slot, int invSlot, int clickData, ClickType actionType) {
		super.slotClicked(slot, invSlot, clickData, actionType);
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
