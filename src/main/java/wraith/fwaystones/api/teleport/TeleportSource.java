package wraith.fwaystones.api.teleport;

import io.wispforest.endec.Endec;

public enum TeleportSource {
    WAYSTONE,
    ABYSS_WATCHER,
    LOCAL_VOID,
    VOID_TOTEM,
    POCKET_WORMHOLE;

    public static final Endec<TeleportSource> ENDEC = Endec.forEnum(TeleportSource.class);
}
