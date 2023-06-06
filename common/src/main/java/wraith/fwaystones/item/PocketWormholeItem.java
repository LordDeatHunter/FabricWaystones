package wraith.fwaystones.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wraith.fwaystones.Waystones;

public class PocketWormholeItem extends Item {
    private static final Component TITLE = Component.translatable("container." + Waystones.MOD_ID + ".pocket_wormhole");
    public PocketWormholeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        //user.openHandledScreen(createScreenHandlerFactory());
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    /*public NamedScreenHandlerFactory createScreenHandlerFactory() {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> new PocketWormholeScreenHandler(i, playerInventory), TITLE);
    }*/
}
