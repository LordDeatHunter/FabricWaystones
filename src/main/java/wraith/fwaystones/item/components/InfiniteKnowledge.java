package wraith.fwaystones.item.components;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.WaystoneDataStorage;

import java.util.function.Consumer;

public record InfiniteKnowledge() implements ExtendedTooltipAppender {
    public static final Endec<InfiniteKnowledge> ENDEC = StructEndec.unit(new InfiniteKnowledge());

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        tooltip.accept(TextUtils.translation("scroll_infinite"));

        var storage = WaystoneDataStorage.getStorageUnsafe();

        int count = (storage != null)
                ? storage.getAllPositions().size()
                : -1;

        if (count != -1) {
            tooltip.accept(TextUtils.translationWithArg("scroll_infinite.tooltip", String.valueOf(count)));
        }
    }
}
