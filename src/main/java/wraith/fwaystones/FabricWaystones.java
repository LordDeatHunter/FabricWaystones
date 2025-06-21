package wraith.fwaystones;

import com.google.common.reflect.Reflection;
import io.wispforest.owo.util.Wisdom;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.fwaystones.api.WaystonePlayerData;
import wraith.fwaystones.api.moss.MossTypes;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.client.registry.WaystoneScreenHandlers;
import wraith.fwaystones.integration.accessories.AccessoriesCompat;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.networking.packets.s2c.SyncWaystoneStorage;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.registry.*;
import wraith.fwaystones.util.FWConfig;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.util.Utils;
import wraith.fwaystones.registry.WaystonesWorldgen;

import java.io.File;

import static wraith.fwaystones.networking.WaystoneNetworkHandler.CHANNEL;

public class FabricWaystones implements ModInitializer {

    public static final FWConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger("Fabric-Waystones");
    public static final String MOD_ID = "fwaystones";

    static {
        var configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "fwaystones/config.json");
        if (configFile.exists()) {
            LOGGER.info("Old config file found, migrating...");
            configFile.renameTo(new File(FabricLoader.getInstance().getConfigDir().toFile(), "fwaystones/config.json5"));
        }
        CONFIG = FWConfig.createAndLoad();
    }

    public static final TagKey<Item> LOCAL_VOID_ITEM = TagKey.of(RegistryKeys.ITEM, id("local_void_item"));
    public static final TagKey<Item> DIRECTED_TELEPORT_ITEM = TagKey.of(RegistryKeys.ITEM, id("directed_teleport_item"));

    public static final TagKey<Item> WAYSTONE_MOSS_APPLIERS = TagKey.of(Registries.ITEM.getKey(), id("waystone_moss"));

    public static final TagKey<Item> WAYSTONE_CLEANERS = TagKey.of(Registries.ITEM.getKey(), id("cleans_waystones"));
    public static final TagKey<Item> WAYSTONE_BUCKET_CLEANERS = TagKey.of(Registries.ITEM.getKey(), id("cleans_waystones_bucket"));

    public static final SoundEvent WAYSTONE_INITIALIZE = registerSoundEvent(id("block.waystone.initialize"));
    public static final SoundEvent WAYSTONE_ACTIVATE = registerSoundEvent(id("block.waystone.activate"));

    public static final SoundEvent WAYSTONE_DEATIVATE = registerSoundEvent(id("block.waystone.deactivate"));
    public static final SoundEvent WAYSTONE_DEACTIVATE2 = registerSoundEvent(id("block.waystone.deactivate2"));

    public static final SoundEvent WAYSTONE_TELEPORT_PLAYER = registerSoundEvent(id("block.waystone.teleport_player"));

    public static final SoundEvent WAYSTONE_MOSS_APPLY = registerSoundEvent(id("block.waystone.moss_apply"));
    public static final SoundEvent WAYSTONE_SHEAR = registerSoundEvent(id("block.waystone.shear"));
    public static final SoundEvent WAYSTONE_CLEAN_SPONGE = registerSoundEvent(id("block.waystone.clean_sponge"));
    public static final SoundEvent WAYSTONE_CLEAN_BUCKET = registerSoundEvent(id("block.waystone.clean_bucket"));
    public static final SoundEvent WAYSTONE_CLEAN_BUCKET_STEAL = registerSoundEvent(id("block.waystone.clean_bucket_steal"));


    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Wraith Waystones is initializing.");

        WaystoneDataComponents.init();

        WaystoneBlocks.init();
        WaystoneBlockEntities.init();
        WaystoneItems.init();

        WaystoneCompatEntries.init();
        WaystoneScreenHandlers.init();

        WaystoneNetworkHandler.init();

        registerEvents();

        Reflection.initialize(WaystonePlayerData.class, WaystoneDataStorage.class);

        WaystoneComponentEventHooks.init();

        WaystoneParticles.init();

        LOGGER.info("Wraith Waystones has successfully been initialized. \n Here take some wisdom: ");
        Wisdom.spread();

        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            AccessoriesCompat.init();
        }

        Reflection.initialize(MossTypes.class, WaystoneTypes.class);
    }

    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            WaystoneDataStorage.getStorage(server).setupServerStorage(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            var storage = WaystoneDataStorage.getStorage(server);
            if (!storage.isSetup()) {
                if (server.isDedicated()) {
                    LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                }
                return;
            }
            storage.reset();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CHANNEL.serverHandle(handler.player).send(new SyncWaystoneStorage(WaystoneDataStorage.getStorage(server)));
        });
        ServerLifecycleEvents.SERVER_STARTING.register(WaystonesWorldgen::registerVanillaVillageWorldgen);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> CONFIG.load());

        var afterCommonRespawnPhase = id("after_common_respawn_phase");

        ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(Event.DEFAULT_PHASE, afterCommonRespawnPhase);
        ServerPlayerEvents.AFTER_RESPAWN.register(afterCommonRespawnPhase, (oldPlayer, newPlayer, alive) -> {
            // Required due to mods possibly causing a desync between server and client as transfer of data attachments may have not occured yet
            // as fabric dose such after respawn which is after entity load event
            WaystonePlayerData.getData(newPlayer).syncDataChange();
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if(!joined) return;

            WaystonePlayerData.getData(player).syncDataChange();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal(MOD_ID)
                .then(CommandManager.literal("delete")
                        .requires(source -> source.hasPermissionLevel(1))
                        .executes(context -> {
                            var player = context.getSource().getPlayer();
                            var storage = WaystoneDataStorage.getStorage(context.getSource().getWorld());
                            if (player == null || storage.isSetup()) return 1;

                            var dimension = Utils.getDimensionName(player.getWorld());
                            storage.removeAllFromWorld(dimension);
                            player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3Removed all waystones from " + dimension + "!"), false);
                            return 1;
                        })
                )
                .then(CommandManager.literal("forget_all")
                        .executes(context -> {
                            var player = context.getSource().getPlayer();
                            if (player == null) return 1;

                            WaystonePlayerData.getData(player).forgetAllWaystones();
                            player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten!"), false);
                            return 1;
                        })
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .requires(source -> source.hasPermissionLevel(1))
                                .executes(context -> {
                                    var player = context.getSource().getPlayer();
                                    if (player == null) return 1;

                                    var target = EntityArgumentType.getPlayer(context, "player");
                                    if (target == null) return 1;

                                    WaystonePlayerData.getData(target).forgetAllWaystones();
                                    player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten for " + target.getName() + "!"), false);
                                    return 1;
                                })
                        )
                )
        ));
    }

    private static SoundEvent registerSoundEvent(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
