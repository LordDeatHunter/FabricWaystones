package wraith.fwaystones.item.components;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.api.core.WaystoneData;

import java.util.function.Consumer;

public record WaystoneDataHolder(WaystoneData data) implements ExtendedTooltipAppender {
    public static final StructEndec<WaystoneDataHolder> ENDEC = StructEndecBuilder.of(
            WaystoneData.ENDEC.flatFieldOf(WaystoneDataHolder::data),
            WaystoneDataHolder::new
    );

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        var holder = stack.get(WaystoneDataComponents.DATA_HOLDER);

        if (holder == null) return;

        Text name = holder.data().name();
        boolean global = holder.data().global();
        tooltip.accept(Text.translatable(
                "fwaystones.waystone_tooltip.name",
                Text.empty().append(name).styled(style ->
                        style.withColor(TextColor.parse(Text.translatable("fwaystones.waystone_tooltip.name.arg_color").getString()).getOrThrow())
                )
        ));
        tooltip.accept(Text.translatable("fwaystones.waystone_tooltip.global").append(" ")
                .append(Text.translatable("fwaystones.waystone_tooltip.global_" + (global ? "on" : "off"))));
    }
}
