package wraith.fwaystones.util;

import io.wispforest.endec.Endec;

public enum TeleportSources {
    WAYSTONE,
    ABYSS_WATCHER,
    LOCAL_VOID,
    VOID_TOTEM,
    POCKET_WORMHOLE;

    public static final Endec<TeleportSources> ENDEC = Endec.forEnum(TeleportSources.class);
}
