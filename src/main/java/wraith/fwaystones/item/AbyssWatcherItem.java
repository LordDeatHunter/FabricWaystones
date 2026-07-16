package wraith.fwaystones.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.screen.AbyssScreenHandler;

public class AbyssWatcherItem extends Item {

    private static final Component TITLE = Component.translatable("container." + FabricWaystones.MOD_ID + ".abyss_watcher");

    public AbyssWatcherItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.openMenu(createScreenHandlerFactory());
        return InteractionResult.CONSUME;
    }

    public MenuProvider createScreenHandlerFactory() {
        return new SimpleMenuProvider((i, playerInventory, playerEntity) -> new AbyssScreenHandler(i, playerInventory), TITLE);
    }

}
