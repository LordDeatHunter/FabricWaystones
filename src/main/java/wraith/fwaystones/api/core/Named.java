package wraith.fwaystones.api.core;

import net.minecraft.text.Text;
import wraith.fwaystones.util.Utils;

import java.util.Locale;

public interface Named {

    void name(String value);

    String name();

    default Text parsedName() {
        return Utils.formatWaystoneName(name());
    }

    default String sortingName() {
        return parsedName().getString().toLowerCase(Locale.ROOT);
    }
}
