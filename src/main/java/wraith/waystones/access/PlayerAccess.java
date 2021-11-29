package wraith.waystones.access;

import java.util.ArrayList;

public interface PlayerAccess {

    ArrayList<String> getHashesSorted();
    int getDiscoveredCount();

}
