package wraith.fwaystones.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import wraith.fwaystones.util.Utils;

public class PocketWormholeScreen extends UniversalScreen{
	private static final ResourceLocation TEXTURE = Utils.ID("textures/gui/pocket_wormhole.png");

	public PocketWormholeScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, TEXTURE, title);
	}

	@Override
	protected void renderCostText(PoseStack matrices, int x, int y, MutableComponent text) {
		renderCostText(matrices, x, y, text, 0x7E3483);
	}


	@Override
	protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
		this.font.draw(matrices, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 0x7E3483);
	}


}
