package wraith.fwaystones.item.components;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

public record WaystoneTeleporter(boolean oneTimeUse) {
    public static final StructEndec<WaystoneTeleporter> ENDEC = StructEndecBuilder.of(
            Endec.BOOLEAN.fieldOf("one_time_use", WaystoneTeleporter::oneTimeUse),
            WaystoneTeleporter::new
    );
}
