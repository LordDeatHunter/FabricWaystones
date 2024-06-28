package wraith.fwaystones.item;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.List;

public class WaystoneItem extends BlockItem {

    public WaystoneItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
//        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        NbtCompound tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA).getNbt();
        if (tag == null) {
            return;
        }
        String name = tag.getString("waystone_name");
        boolean global = tag.getBoolean("waystone_is_global");
        tooltip.add(Text.translatable(
            "fwaystones.waystone_tooltip.name",
            Text.literal(name).styled(style ->
                style.withColor(TextColor.parse(Text.translatable("fwaystones.waystone_tooltip.name.arg_color").getString()).getOrThrow())
            )
        ));
        tooltip.add(Text.translatable("fwaystones.waystone_tooltip.global").append(" ")
            .append(Text.translatable("fwaystones.waystone_tooltip.global_" + (global ? "on" : "off"))));
    }

}
