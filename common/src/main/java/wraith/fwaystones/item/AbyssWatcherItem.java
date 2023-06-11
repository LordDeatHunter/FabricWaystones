package wraith.fwaystones.item;


import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.screen.AbyssScreenHandler;

public class AbyssWatcherItem extends Item {
    private static final Component TITLE = Component.translatable("container." + Waystones.MOD_ID + ".abyss_watcher");

    public AbyssWatcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()){
            player.openMenu(new SimpleMenuProvider((id, inventory, player2) -> new AbyssScreenHandler(id, inventory), TITLE));
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
