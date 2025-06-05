package wraith.fwaystones.item.components;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ExtendedTooltipAppender extends TooltipAppender {
    default void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

    }

    void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type);
}
