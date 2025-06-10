package wraith.fwaystones.api;

import io.wispforest.owo.ui.core.Color;
import net.minecraft.util.Identifier;

public record WaystoneType(
    Identifier iconId,
    int defaultColor
) {
}
