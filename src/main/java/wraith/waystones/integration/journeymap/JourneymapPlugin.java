package wraith.waystones.integration.journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Waystones;
import wraith.waystones.access.WaystoneValue;
import wraith.waystones.integration.event.WaystoneEvents;
import wraith.waystones.util.Utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STARTED;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STOPPED;
import static journeymap.client.api.event.ClientEvent.Type.REGISTRY;

public class JourneymapPlugin implements IClientPlugin
{
    // API reference
    private IClientAPI api = null;
    private static JourneymapPlugin INSTANCE;
    private BooleanOption enabled;

    private List<WaystoneValue> queuedWaypoints;

    private boolean mappingStarted = false;

    /**
     * Do not manually instantiate this class, Journeymap will take care of it.
     */
    public JourneymapPlugin()
    {
        INSTANCE = this;
        queuedWaypoints = new ArrayList<>();
    }

    /**
     * This will return null if Journeymap is not loaded. Use carefully.
     *
     * @return - The Plugin
     */
    @Nullable
    public static JourneymapPlugin getInstance()
    {
        return INSTANCE;
    }

    /**
     * This is called very early in the mod loading life cycle if Journeymap is loaded.
     *
     * @param api Client API implementation
     */
    @Override
    public void initialize(IClientAPI api)
    {
        this.api = api;

        // event registration
        api.subscribe(getModId(), EnumSet.of(REGISTRY, MAPPING_STOPPED, MAPPING_STARTED));
        WaystoneEvents.REMOVE_WAYSTONE_EVENT.register(this::onRemove);
        WaystoneEvents.DISCOVER_WAYSTONE_EVENT.register(this::onDiscover);
        WaystoneEvents.RENAME_WAYSTONE_EVENT.register(this::onRename);
    }


    @Override
    public String getModId()
    {
        return Waystones.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event)
    {
        try
        {
            switch (event.type)
            {
                case MAPPING_STARTED ->
                {
                    mappingStarted = true;
                    buildQueuedWaypoints();
                }
                case MAPPING_STOPPED ->
                {
                    mappingStarted = false;
                    api.removeAll(Waystones.MOD_ID);
                }
                case REGISTRY ->
                {
                    RegistryEvent registryEvent = (RegistryEvent) event;
                    switch (registryEvent.getRegistryType())
                    {
                        case OPTIONS ->
                        {
                            OptionCategory category = new OptionCategory(Waystones.MOD_ID, "waystones.integration.journeymap.category");
                            this.enabled = new BooleanOption(category, "enabled", "waystones.integration.journeymap.enable", true, true);
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            Waystones.LOGGER.error(t.getMessage(), t);
        }

    }

    private void buildQueuedWaypoints()
    {
        queuedWaypoints.forEach(this::addWaypoint);
        queuedWaypoints.clear();
    }

    private void onRemove(String hash)
    {
        if (enabled.get())
        {
            Waypoint waypoint = api.getWaypoint(getModId(), hash);
            if (waypoint != null)
            {
                api.remove(waypoint);
            }
        }
    }

    private void onDiscover(WaystoneValue waystone)
    {
        if (waystone != null && enabled.get())
        {
            if (mappingStarted)
            {
                addWaypoint(waystone);
            }
            else
            {
                queuedWaypoints.add(waystone);
            }

        }
    }

    private void onRename(WaystoneValue waystone)
    {
        if (waystone != null && enabled.get())
        {
            onRemove(waystone.getHash());
            addWaypoint(waystone);
        }
    }

    public void addWaypoint(WaystoneValue waystone)
    {
        if (Waystones.WAYSTONE_STORAGE == null)
        {
            return;
        }

        var icon = new MapImage(Utils.ID("images/waystone-icon.png"), 16, 16); // this image will be very large until journeymap 5.8.4 is released

        var waypoint = new Waypoint(
                getModId(),
                waystone.getHash(),
                waystone.getWaystoneName(),
                waystone.getWorldName(),
                waystone.way_getPos()
        )
                .setIcon(icon)
                .setPersistent(false);

        try
        {
            this.api.show(waypoint);
        }
        catch (Throwable t)
        {
            Waystones.LOGGER.error(t.getMessage(), t);
        }
    }
}
