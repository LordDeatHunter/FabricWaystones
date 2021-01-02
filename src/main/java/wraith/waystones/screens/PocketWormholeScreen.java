package wraith.waystones.screens;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.waystones.Utils;


public class PocketWormholeScreen extends UniversalWaystoneScreen {

    private static final Identifier TEXTURE = Utils.ID("textures/gui/container/pocket_wormhole.png");

    public PocketWormholeScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, TEXTURE, title);
        this.backgroundWidth = 177;
        this.backgroundHeight = 110;
    }

}