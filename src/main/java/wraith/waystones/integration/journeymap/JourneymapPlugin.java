package wraith.waystones.integration.journeymap;

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
import wraith.waystones.integration.event.WaystoneEvents;
import wraith.waystones.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static journeymap.client.api.event.ClientEvent.Type.*;

public class JourneymapPlugin implements IClientPlugin {

    private final List<String> queuedWaypoints;
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
            Waystones.WAYSTONE_STORAGE.getAllHashes().forEach(this::addWaypoint);
        }
    }

    private void buildQueuedWaypoints() {
        queuedWaypoints.forEach(this::addWaypoint);
        queuedWaypoints.clear();
    }

    private void onRemove(final String hash) {
        if (!enabled.get()) {
            return;
        }
        Waypoint waypoint = api.getWaypoint(getModId(), hash);
        if (waypoint != null) {
            api.remove(waypoint);
        }
    }

    private void onDiscover(String hash) {
        if (Waystones.WAYSTONE_STORAGE == null) {
            return;
        }
        if (!enabled.get()) return;
        if (mappingStarted) {
            addWaypoint(hash);
        } else {
            // queue waypoints to be displayed once mapping has started.
            // Waystones that are sent on server join is too early to be displayed, so we need to wait.
            queuedWaypoints.add(hash);
        }
    }

    private void onRename(String hash) {
        onRemove(hash);
        addWaypoint(hash);
    }

    private void addWaypoint(String hash) {
        if (Waystones.WAYSTONE_STORAGE == null || api.getWaypoint(getModId(), hash) != null) {
            return; // do not recreate waypoint
        }
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystoneData(hash);
        if (waystone == null) {
            return;
        }
        var icon = new MapImage(Utils.ID("images/waystone-icon.png"),
            16, 16);

        var waypoint = new Waypoint(
            getModId(),
            waystone.getHash(),
            waystone.getWaystoneName(),
            waystone.getWorldName(),
            waystone.way_getPos()
        )
            .setIcon(icon)
            .setPersistent(false);

        try {
            var color = waystone.getColor();
            if (color == null && randomizeColor.get()) {
                color = getRandomColor();
                Waystones.WAYSTONE_STORAGE.recolorWaystone(hash, color);
            }
            if (color != null) {
                waypoint.setColor(color);
            }

            waypoint.setEnabled(displayWaypoints.get());
            this.api.show(waypoint);
        } catch (Throwable t) {
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
