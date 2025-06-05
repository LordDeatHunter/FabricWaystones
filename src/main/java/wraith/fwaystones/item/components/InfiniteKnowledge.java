package wraith.fwaystones.item.components;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.function.Consumer;

public record InfiniteKnowledge() implements ExtendedTooltipAppender {
    public static final Endec<InfiniteKnowledge> ENDEC = StructEndec.unit(new InfiniteKnowledge());

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        tooltip.accept(Text.translatable("fwaystones.scroll.infinite"));

        var storage = WaystoneDataStorage.getStorageUnsafe();

        int count = (storage != null)
                ? storage.getAllPositions().size()
                : -1;

        if (count != -1) {
            tooltip.accept(Text.translatable(
                    "fwaystones.scroll.infinite_tooltip",
                    Text.literal(String.valueOf(count)).styled(style ->
                            style.withColor(TextColor.parse(Text.translatable("fwaystones.scroll.infinite_tooltip.arg_color").getString()).getOrThrow())
                    )
            ));
        }
    }
}
