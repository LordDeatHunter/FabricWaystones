package wraith.waystones.screen;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;


public class PocketWormholeScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/pocket_wormhole.png");

    public PocketWormholeScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);
    }

    @Override
    protected void renderCostText(MatrixStack matrices, int x, int y, MutableText text) {
        this.textRenderer.draw(matrices, text.append(new LiteralText(": " + Config.getInstance().teleportCost())), x + 20, y + 5, 0x7E3483);
    }


    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0x7E3483);
    }


}