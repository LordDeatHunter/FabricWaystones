package wraith.fwaystones.item.components;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.registry.WaystoneDataComponents;

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

        if (data instanceof NetworkedWaystoneData networkData) {
            var name = networkData.name();
            var global = networkData.global();

            tooltip.accept(TextUtils.translationWithArg("waystone.tooltip.name", name));
            tooltip.accept(TextUtils.translationWithArg("waystone.tooltip.global", TextUtils.translation("waystone.tooltip.global_" + (global ? "on" : "off"))));
        }
    }
}
