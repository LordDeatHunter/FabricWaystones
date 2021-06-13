package wraith.waystones.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaystoneItem extends BlockItem {

    public WaystoneItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag tag = stack.getSubTag("BlockEntityTag");
        if (tag == null) {
            return;
        }
        String name = tag.getString("waystone_name");
        boolean global = tag.getBoolean("waystone_is_global");
        tooltip.add(new TranslatableText("waystones.waystone_tooltip.name").append(" " + name).formatted(Formatting.DARK_AQUA));
        tooltip.add(new TranslatableText("waystones.waystone_tooltip.global").append(" ").append(new TranslatableText("waystones.waystone_tooltip.global_" + (global ? "on" : "off"))).formatted(Formatting.DARK_AQUA));
    }

}
