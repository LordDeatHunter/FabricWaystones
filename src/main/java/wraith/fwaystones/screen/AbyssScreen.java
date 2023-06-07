package wraith.fwaystones.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fwaystones.util.Utils;

public class AbyssScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/abyss.png");

    public AbyssScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        texture = TEXTURE;
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