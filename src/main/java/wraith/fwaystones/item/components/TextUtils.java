package wraith.fwaystones.item.components;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import wraith.fwaystones.FabricWaystones;

public class TextUtils {
    public static Text translationWithArg(String baseTranslationKey, String arg) {
        return translationWithArg(baseTranslationKey, Text.literal(arg));
    }

    public static Text translationWithArg(String baseTranslationKey, Object ...args) {
        var translationKey = translationKey(baseTranslationKey);

        return Text.translatable(translationKey, args);
    }

    public static String translationKey(String path) {
        return FabricWaystones.MOD_ID + "." + path;
    }

    public static MutableText translation(String path) {
        return Text.translatable(translationKey(path));
    }
}
