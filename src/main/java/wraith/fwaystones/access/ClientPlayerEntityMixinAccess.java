package wraith.fwaystones.access;

import java.util.ArrayList;

public interface ClientPlayerEntityMixinAccess extends PlayerAccess {

    void fabricWaystones$requestSync();
    ArrayList<String> fabricWaystones$getWaystonesSorted();

}
