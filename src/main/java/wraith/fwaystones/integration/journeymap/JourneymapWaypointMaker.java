package wraith.fwaystones.integration.journeymap;

import joptsimple.internal.Strings;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.event.MappingEvent;
import journeymap.api.v2.client.option.BooleanOption;
import journeymap.api.v2.client.option.OptionCategory;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.api.core.WaystonePosition;
import wraith.fwaystones.integration.WaystoneWaypointMaker;
import wraith.fwaystones.item.components.TextUtils;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneymapWaypointMaker extends WaystoneWaypointMaker<String> implements IClientPlugin {

    private IClientAPI api = null;

    @Nullable
    private BooleanOption enabled = null;
    @Nullable
    private BooleanOption displayWaypoints = null;

    private boolean mappingStarted = false;

    //--

    @Override
    @Nullable
    protected WaystonePosition deleteWaypointFromPosition(String waypointId) {
        var waypoint = api.getWaypoint(getModId(), waypointId);

        if (waypoint != null) {
            api.removeWaypoint(getModId(), waypoint);
        }

        return super.deleteWaypointFromPosition(waypointId);
    }

    @Override
    public boolean validToCreateWaypoints() {
        return mappingStarted;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled != null ? this.enabled.get() : true;
    }

    @Override
    public boolean displayWaypoints() {
        return this.displayWaypoints != null ? displayWaypoints.get() : true;
    }

    @Override
    public Identifier getId() {
        return FabricWaystones.id("journeymap");
    }

    @Override
    public String createWaypoint(WaystonePosition pos, WaystoneData data) {
        var group = this.api.getWaypointGroupByName(FabricWaystones.MOD_ID, "Waystones");

        if (group == null) {
            group = WaypointFactory.createWaypointGroup(FabricWaystones.MOD_ID, "Waystones"); // might want to add this to i18n
            group.setLocked(true); // so users cannot move waypoints in and out of the group(can still delete the group, but will be recreated on game join.)
        }

        // TODO: WILL THIS BE NEEDED?
        var waypointId = uuidToPoint.get(data.uuid());
        var oldWaypoint = api.getWaypoint(getModId(), waypointId);
        if (oldWaypoint != null) return oldWaypoint.getGuid();

        var name = data.nameAsString();

        var waypoint = WaypointFactory.createClientWaypoint(
                getModId(),
                pos.blockPos(),
                Strings.isNullOrEmpty(name) ? "Unnamed Waystone" : name,
                pos.worldName(),
                false
        );

        waypoint.setIconResourceLoctaion(Identifier.of(FabricWaystones.MOD_ID, "images/fabric_waystones_icon.png"));

        try {
            waypoint.setColor(data.color());
            waypoint.setIconColor(null); // so icon is not colored
            waypoint.setEnabled(displayWaypoints.get());
            this.api.addWaypoint(getModId(), waypoint);

            group.setLocked(false); // needed for now until JM is updated, cannot add waypoints to locked groups in code currently. So we need to unlock it and lock it.
            group.addWaypoint(waypoint);
            group.setLocked(true);
        } catch (Throwable t) {
            FabricWaystones.LOGGER.error("Unable to setup a Waypoints extra data for JourneyMap Compat due to an error", t);
        }

        return waypoint.getGuid();
    }

    //--

    @Override
    public String getModId() {
        return FabricWaystones.MOD_ID;
    }

    @Override
    public void initialize(IClientAPI api) {
        this.api = api;

        // event registration
        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe(getModId(), event -> {
            OptionCategory category = new OptionCategory(getModId(), "fwaystones.integration.journeymap.category");
            this.enabled = new BooleanOption(category, "enabled", "fwaystones.integration.journeymap.enable", true);
            this.displayWaypoints = new BooleanOption(category, "displayed", "fwaystones.integration.journeymap.waypoints_enable", true);
        });

        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(getModId(), event -> {
            event.getThemeButtonDisplay()
                    .addThemeToggleButton(
                            TextUtils.translationKey("integration.journeymap.theme.on"),
                            TextUtils.translationKey("integration.journeymap.theme.off"),
                            FabricWaystones.id("fabric_waystones_icon.png"),
                            displayWaypoints.get(),
                            b -> {
                                b.toggle();
                                displayWaypoints.set(b.getToggled());
                                resetWaystones();
                            });
        });

        ClientEventRegistry.MAPPING_EVENT.subscribe(getModId(), event -> {
            if (event.getStage().equals(MappingEvent.Stage.MAPPING_STARTED)) {
                mappingStarted = true;
                shouldResetAllWaypoints = true;
            } else {
                deleteAllWaypoints();

                mappingStarted = false;

                api.removeAll(getModId());
            }
        });
    }
}
