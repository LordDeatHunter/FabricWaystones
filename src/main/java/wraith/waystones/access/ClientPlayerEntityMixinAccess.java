package wraith.waystones.access;

import java.util.ArrayList;

public interface ClientPlayerEntityMixinAccess extends PlayerAccess {

    void requestSync();
    ArrayList<String> getWaystonesSorted();

}
