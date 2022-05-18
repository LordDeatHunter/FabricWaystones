package wraith.waystones.integration.journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Waystones;
import wraith.waystones.access.WaystoneValue;
import wraith.waystones.integration.event.WaystoneEvents;

import java.util.EnumSet;

import static journeymap.client.api.event.ClientEvent.Type.REGISTRY;

public class JourneymapPlugin implements IClientPlugin
{
    // API reference
    private IClientAPI api = null;
    private static JourneymapPlugin INSTANCE;
    private OptionCategory category;
    private BooleanOption enabled;

    /**
     * Do not manually instantiate this class, Journeymap will take care of it.
     */
    public JourneymapPlugin()
    {
        INSTANCE = this;
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
        api.subscribe(getModId(), EnumSet.of(REGISTRY));
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
                case REGISTRY:
                    RegistryEvent registryEvent = (RegistryEvent) event;
                    switch (registryEvent.getRegistryType())
                    {
                        case OPTIONS:
                            this.category = new OptionCategory(Waystones.MOD_ID, "waystones.integration.journeymap.category");
                            this.enabled = new BooleanOption(category, "enabled", "waystones.integration.journeymap.enable", true, true);
                            break;
                    }
                    break;
            }
        }
        catch (Throwable t)
        {
            Waystones.LOGGER.error(t.getMessage(), t);
        }

    }

    private void onRemove(WaystoneValue waystone)
    {
        if (waystone != null && enabled.get())
        {
            Waypoint waypoint = api.getWaypoint(getModId(), waystone.getEntity().getHash());
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

        }
    }

    private void onRename(WaystoneValue waystone, String newName)
    {
        if (waystone != null && enabled.get())
        {
            onRemove(waystone);
        }
    }

}
