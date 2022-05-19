package wraith.waystones.integration.journeymap;

import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STARTED;
import static journeymap.client.api.event.ClientEvent.Type.MAPPING_STOPPED;
import static journeymap.client.api.event.ClientEvent.Type.REGISTRY;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import journeymap.client.api.event.RegistryEvent.RegistryType;
import journeymap.client.api.event.fabric.FabricEvents;
import journeymap.client.api.event.fabric.FullscreenDisplayEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;
import org.jetbrains.annotations.NotNull;
import wraith.waystones.Waystones;
import wraith.waystones.access.WaystoneValue;
import wraith.waystones.integration.event.WaystoneEvents;
import wraith.waystones.util.Utils;

public class JourneymapPlugin implements IClientPlugin {

    private final List<WaystoneValue> queuedWaypoints;
    // API reference
    private IClientAPI api = null;
    private BooleanOption enabled;
    private BooleanOption displayWaypoints;
    private BooleanOption randomizeColor;
    private boolean mappingStarted = false;

    /**
     * Do not manually instantiate this class, Journeymap will take care of it.
     */
    public JourneymapPlugin() {
        this.queuedWaypoints = new ArrayList<>();
    }

    /**
     * This is called very early in the mod loading life cycle if Journeymap is loaded.
     *
     * @param api Client API implementation
     */
    @Override
    public void initialize(IClientAPI api) {
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
     *
     * @param addonButtonDisplayEvent - the event
     */
    private void onFullscreenAddonButton(
        FullscreenDisplayEvent.AddonButtonDisplayEvent addonButtonDisplayEvent) {
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
    public String getModId() {
        return Waystones.MOD_ID;
    }

    @Override
    public void onEvent(@NotNull final ClientEvent event) {
        try {
            switch (event.type) {
                case MAPPING_STARTED -> {
                    mappingStarted = true;
                    buildQueuedWaypoints();
                }
                case MAPPING_STOPPED -> {
                    mappingStarted = false;
                    api.removeAll(getModId());
                }
                case REGISTRY -> {
                    RegistryEvent registryEvent = (RegistryEvent) event;
                    if (registryEvent.getRegistryType()
                        == RegistryType.OPTIONS) {// Adds an option in the journeymap options screen.
                        OptionCategory category = new OptionCategory(getModId(),
                            "waystones.integration.journeymap.category");
                        this.enabled = new BooleanOption(category, "enabled",
                            "waystones.integration.journeymap.enable", true);
                        this.randomizeColor = new BooleanOption(category, "random_color",
                            "waystones.integration.journeymap.random_color", true);
                        // hidden from the ui
                        this.displayWaypoints = new BooleanOption(
                            new OptionCategory(getModId(), "Hidden"), "displayed",
                            "waystones.integration.journeymap.enable", true);
                    }
                }
            }
        } catch (Throwable t) {
            Waystones.LOGGER.error(t.getMessage(), t);
        }

    }

    private void updateWaypointDisplay(boolean display) {
        if (!display) {
            api.removeAll(getModId(), DisplayType.Waypoint);
        } else {
            Waystones.WAYSTONE_STORAGE.getAllHashes()
                .forEach(hash -> addWaypoint(Waystones.WAYSTONE_STORAGE.getWaystoneData(hash)));
        }
    }

    private void buildQueuedWaypoints() {
        queuedWaypoints.forEach(this::addWaypoint);
        queuedWaypoints.clear();
    }

    private void onRemove(final String hash) {
        if (enabled.get()) {
            Waypoint waypoint = api.getWaypoint(getModId(), hash);
            Waypoint diskWp = api.getWaypoints(getModId()).stream().filter(w ->
                    w.getId().equals(hash)).findFirst().orElse(null);
            if (waypoint != null) {
                api.remove(waypoint);
            }
            // failsafe in-case waypoint is on disk and not loaded in plugin memory.
            if (diskWp != null) {
                api.remove(diskWp);
            }
        }
    }

    private void onDiscover(String hash) {
        if (Waystones.WAYSTONE_STORAGE == null) {
            return;
        }
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone != null && enabled.get()) {
            if (mappingStarted) {
                addWaypoint(waystone);
            } else {
                // queue waypoints to be displayed once mapping has started.
                // Waystones that are sent on server join is too early to be displayed so we need to wait.
                queuedWaypoints.add(waystone);
            }

        }
    }

    private void onRename(String hash) {
        if (Waystones.WAYSTONE_STORAGE == null) {
            return;
        }
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        Integer color = 0;
        Waypoint waypoint = api.getWaypoint(getModId(), waystone.getHash());

        if (waypoint != null)
        {
            //save the old color!
            color = waypoint.getColor();
        }
        onRemove(waystone.getHash());
        addWaypoint(waystone, color);
    }

    private void addWaypoint(WaystoneValue waystone) {
        addWaypoint(waystone, null);
    }

    private void addWaypoint(WaystoneValue waystone, Integer color) {
        if (Waystones.WAYSTONE_STORAGE == null
            || api.getWaypoint(getModId(), waystone.getHash()) != null) // do not recreate waypoint
        {
            return;
        }

        var icon = new MapImage(Utils.ID("images/waystone-icon.png"),
                16, 16); // this image will be very large until journeymap 5.8.4 is released

        var waypoint = new Waypoint(
            getModId(),
            waystone.getHash(),
            waystone.getWaystoneName(),
            waystone.getWorldName(),
            waystone.way_getPos()
        )
            .setIcon(icon);

        try {
            if (randomizeColor.get() && color == null) {
                color = getRandomColor();
            }

            if (color != null) {
                waypoint.setColor(color);
            }

            waypoint.setEnabled(displayWaypoints.get());
            this.api.show(waypoint);
        }
        catch (Throwable t) {
            Waystones.LOGGER.error(t.getMessage(), t);
        }
    }

    private int getRandomColor() {
        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        return new Color(r, g, b).getRGB();
    }
}
