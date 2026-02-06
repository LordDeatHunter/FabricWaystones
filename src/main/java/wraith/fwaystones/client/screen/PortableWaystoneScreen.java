package wraith.fwaystones.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import wraith.fwaystones.FabricWaystones;

public class PortableWaystoneScreen extends UniversalWaystoneScreen<PortableWaystoneScreenHandler> {

    public PortableWaystoneScreen(PortableWaystoneScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        texture = handler.isAbyssal()
                ? FabricWaystones.id("textures/gui/abyss.png")
                : FabricWaystones.id("textures/gui/pocket_wormhole.png");
    }

    @Override
    protected void renderCostText(DrawContext context, int x, int y, MutableText text) {
        renderCostText(context, x, y, text, 0x7E3483);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, this.title, this.titleX, this.titleY, 0x7E3483, false);
    }
}
