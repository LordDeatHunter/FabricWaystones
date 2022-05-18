package wraith.waystones.integration.journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.event.fabric.FabricEvents;
import journeymap.client.api.event.fabric.FullscreenDisplayEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;
import org.jetbrains.annotations.NotNull;
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
    private BooleanOption enabled;
    private BooleanOption displayWaypoints;

    private final List<WaystoneValue> queuedWaypoints;

    private boolean mappingStarted = false;

    /**
     * Do not manually instantiate this class, Journeymap will take care of it.
     */
    public JourneymapPlugin()
    {
        this.queuedWaypoints = new ArrayList<>();
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
        FabricEvents.ADDON_BUTTON_DISPLAY_EVENT.register(this::onFullscreenAddonButton);
    }

    /**
     * Adds a themeable button in the fullscreen map to toggle waystone waypoint display.
     * @param addonButtonDisplayEvent - the event
     */
    private void onFullscreenAddonButton(FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent)
    {
        addonButtonDisplayEvent.getThemeButtonDisplay()
                .addThemeToggleButton(
                        "waystones.integration.journeymap.theme.on",
                        "waystones.integration.journeymap.theme.off",
                        "waystone-icon", // required to be in assets/journeymap/flat/icon due to themes
                        displayWaypoints.get(),
                        b -> {
                            b.toggle();
                            displayWaypoints.set(b.getToggled());
                            updateWaypointDisplay(b.getToggled());
                        });

    }

    @Override
    public String getModId()
    {
        return Waystones.MOD_ID;
    }

    @Override
    public void onEvent(@NotNull final ClientEvent event)
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
                    api.removeAll(getModId());
                }
                case REGISTRY ->
                {
                    RegistryEvent registryEvent = (RegistryEvent) event;
                    switch (registryEvent.getRegistryType())
                    {
                        case OPTIONS ->
                        {
                            // Adds an option in the journeymap options screen.
                            OptionCategory category = new OptionCategory(getModId(), "waystones.integration.journeymap.category");
                            this.enabled = new BooleanOption(category, "enabled", "waystones.integration.journeymap.enable", true);
                            // hidden from the ui
                            this.displayWaypoints = new BooleanOption(new OptionCategory(getModId(), "Hidden"), "displayed", "waystones.integration.journeymap.enable", true);
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

    private void updateWaypointDisplay(boolean display) {
       if(!display) {
           api.removeAll(getModId(), DisplayType.Waypoint);
       } else {
           Waystones.WAYSTONE_STORAGE.getAllHashes().forEach(hash-> addWaypoint(Waystones.WAYSTONE_STORAGE.getWaystoneData(hash)));
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
                // queue waypoints to be displayed once mapping has started.
                // Waystones that are sent on server join is too early to be displayed so we need to wait.
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
                .setPersistent(false);  // we don't want to save to disk.

        try
        {
            waypoint.setEnabled(displayWaypoints.get());
            this.api.show(waypoint);
        }
        catch (Throwable t)
        {
            Waystones.LOGGER.error(t.getMessage(), t);
        }
    }
}
