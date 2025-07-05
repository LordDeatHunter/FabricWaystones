package wraith.fwaystones.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.item.components.WaystoneTyped;
import wraith.fwaystones.registry.WaystoneDataComponents;

public class WaystoneBlockItem extends BlockItem {
    public WaystoneBlockItem(Block block, Settings settings) {
        super(block, settings.component(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(WaystoneTypes.STONE)));
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        var data = stack.get(WaystoneDataComponents.WAYSTONE_TYPE);
        if (data != null) {
            var id = data.getType().getId();
            if (id != null) return String.join(
                ".",
                super.getTranslationKey(stack),
                id.toTranslationKey()
            );
        }
        return super.getTranslationKey(stack);
    }
}
