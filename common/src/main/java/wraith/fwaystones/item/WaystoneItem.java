package wraith.fwaystones.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WaystoneItem extends BlockItem {
    public WaystoneItem(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag == null) {
            return;
        }
        String name = tag.getString("waystone_name");
        boolean global = tag.getBoolean("waystone_is_global");
        tooltip.add(Component.translatable(
                "fwaystones.waystone_tooltip.name",
                Component.literal(name).withStyle(style ->
                        style.withColor(TextColor.parseColor(Component.translatable("fwaystones.waystone_tooltip.name.arg_color").getString()))
                )
        ));
        tooltip.add(Component.translatable("fwaystones.waystone_tooltip.global").append(" ")
                .append(Component.translatable("fwaystones.waystone_tooltip.global_" + (global ? "on" : "off"))));
    }
}
