package wraith.waystones.screens;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.waystones.Config;
import wraith.waystones.Utils;

public class AbyssScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/abyss.png");

    public AbyssScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);
        this.backgroundWidth = 177;
        this.backgroundHeight = 110;
    }

    @Override
    public void renderCostText(MatrixStack matrices, int x, int y, String text) {
        this.textRenderer.draw(matrices, text + ": " + Config.getInstance().teleportCost(), x + 20, y + 5, 0x7E3483);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0x7E3483);
    }

}