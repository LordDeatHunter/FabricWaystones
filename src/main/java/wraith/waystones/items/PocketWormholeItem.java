package wraith.waystones.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import wraith.waystones.Waystones;
import wraith.waystones.screens.PocketWormholeScreenHandler;

public class PocketWormholeItem extends Item {

    private static final Text TITLE = new TranslatableText("container." + Waystones.MOD_ID + ".pocket_wormhole");

    public PocketWormholeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.openHandledScreen(createScreenHandlerFactory());
        return TypedActionResult.consume(user.getMainHandStack());
    }

    public NamedScreenHandlerFactory createScreenHandlerFactory() {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> new PocketWormholeScreenHandler(i, playerInventory), TITLE);
    }

}
