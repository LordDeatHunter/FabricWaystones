package wraith.fwaystones.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import wraith.fwaystones.util.Utils;

public class AbyssScreen extends UniversalWaystoneScreen {

    private static final ResourceLocation TEXTURE = Utils.ID("textures/gui/abyss.png");

    public AbyssScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        texture = TEXTURE;
    }


    @Override
    protected void renderCostText(GuiGraphics context, int x, int y, MutableComponent text) {
        renderCostText(context, x, y, text, 0xFF7E3483);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        context.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF7E3483, false);
    }

}