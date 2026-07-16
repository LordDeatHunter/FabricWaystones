package wraith.fwaystones.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public class WaystonesEventManager {

    private WaystonesEventManager() {}

    public static void registerEvents() {

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                FabricWaystones.WAYSTONE_STORAGE = new WaystoneStorage(server);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                if (server.isDedicatedServer())
                    FabricWaystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            FabricWaystones.WAYSTONE_STORAGE.saveWaystones(false);
            FabricWaystones.WAYSTONE_STORAGE = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> FabricWaystones.WAYSTONE_STORAGE.sendToPlayer(handler.player));
        ServerLifecycleEvents.SERVER_STARTING.register(WaystonesWorldgen::registerVanillaVillageWorldgen);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> FabricWaystones.CONFIG.load());

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ((PlayerEntityMixinAccess) newPlayer).fabricWaystones$syncData());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal(FabricWaystones.MOD_ID)
            .then(Commands.literal("delete")
                .requires(source -> source.hasPermission(1))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null || FabricWaystones.WAYSTONE_STORAGE == null) {
                        return 1;
                    }
                    var dimension = Utils.getDimensionName(player.level());
                    FabricWaystones.WAYSTONE_STORAGE.removeWorldWaystones(dimension);
                    player.displayClientMessage(Component.literal("§6[§eFabric Waystones§6] §3Removed all waystones from " + dimension + "!"), false);
                    return 1;
                })
            )
            .then(Commands.literal("forget_all")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    ((PlayerEntityMixinAccess) player).fabricWaystones$forgetAllWaystones();
                    player.displayClientMessage(Component.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten!"), false);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(1))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        ServerPlayer target = EntityArgument.getPlayer(context, "player");
                        if (target == null) {
                            return 1;
                        }
                        ((PlayerEntityMixinAccess) target).fabricWaystones$forgetAllWaystones();
                        player.displayClientMessage(Component.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten for " + target.getName() + "!"), false);
                        return 1;
                    })
                )
            )
        ));
    }

}