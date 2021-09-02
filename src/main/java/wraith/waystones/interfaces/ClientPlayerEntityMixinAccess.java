package wraith.waystones.interfaces;

import java.util.ArrayList;

public interface ClientPlayerEntityMixinAccess extends PlayerAccess {

    void requestSync();
    ArrayList<String> getWaystonesSorted();

}
