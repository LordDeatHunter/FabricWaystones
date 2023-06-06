package wraith.fwaystones.access;

import java.util.ArrayList;

public interface ClientPlayerEntityMixinAccess extends PlayerAccess {

    void requestSync();
    ArrayList<String> getWaystonesSorted();

}
