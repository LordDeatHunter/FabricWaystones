package wraith.fwaystones.item.components;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.util.Identifier;
import wraith.fwaystones.api.core.WaystoneType;
import wraith.fwaystones.api.core.WaystoneTypes;

public record WaystoneTyped(Identifier id) {
    public static final StructEndec<WaystoneTyped> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("id", WaystoneTyped::id),
            WaystoneTyped::new
    );

    public static final WaystoneTyped DEFAULT = new WaystoneTyped(WaystoneTypes.STONE);

    public WaystoneType getType() {
        return WaystoneTypes.getTypeOrDefault(id);
    }
}
