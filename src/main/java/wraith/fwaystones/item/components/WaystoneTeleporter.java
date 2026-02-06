package wraith.fwaystones.item.components;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;

import java.util.function.Consumer;

public record WaystoneTeleporter(boolean oneTimeUse) implements ExtendedTooltipAppender {
    public static final StructEndec<WaystoneTeleporter> ENDEC = StructEndecBuilder.of(
            Endec.BOOLEAN.fieldOf("one_time_use", WaystoneTeleporter::oneTimeUse),
            WaystoneTeleporter::new
    );

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        if (stack == null) return;

        var cooldowns = FabricWaystones.CONFIG.teleportCooldowns;

        var cooldownAmount = oneTimeUse ? cooldowns.usedAbyssWatcher() : cooldowns.usedPockedWormhole();

        if(cooldownAmount > 0) {
            tooltip.accept(TextUtils.translationWithArg("cool_down.tooltip", String.valueOf(cooldownAmount / 20)));
        }
    }
}
