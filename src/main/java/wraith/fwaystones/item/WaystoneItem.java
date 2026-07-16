package wraith.fwaystones.item;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class WaystoneItem extends BlockItem {

    public WaystoneItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        CustomData component = stack.get(DataComponents.CUSTOM_DATA);
        if (component == null) {
            return;
        }
        CompoundTag tag = component.copyTag();
        String name = tag.getStringOr("waystone_name", "");
        boolean global = tag.getBooleanOr("waystone_is_global", false);
        tooltip.accept(Component.translatable(
            "fwaystones.waystone_tooltip.name",
            Component.literal(name).withStyle(style ->
                style.withColor(TextColor.parseColor(Component.translatable("fwaystones.waystone_tooltip.name.arg_color").getString()).getOrThrow())
            )
        ));
        tooltip.accept(Component.translatable("fwaystones.waystone_tooltip.global").append(" ")
            .append(Component.translatable("fwaystones.waystone_tooltip.global_" + (global ? "on" : "off"))));
    }

}
