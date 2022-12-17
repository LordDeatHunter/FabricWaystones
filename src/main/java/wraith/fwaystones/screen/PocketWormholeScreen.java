package wraith.fwaystones.screen;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fwaystones.util.Utils;


public class PocketWormholeScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/pocket_wormhole.png");

    public PocketWormholeScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);
    }

    @Override
    protected void renderCostText(MatrixStack matrices, int x, int y, MutableText text) {
        renderCostText(matrices, x, y, text, 0x7E3483);
    }


    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float) this.titleX, (float) this.titleY, 0x7E3483);
    }


}