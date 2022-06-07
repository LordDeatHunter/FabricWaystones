package wraith.fwaystones.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
                if (server.isDedicated())
                    FabricWaystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            FabricWaystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            FabricWaystones.WAYSTONE_STORAGE = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PacketByteBuf data = PacketByteBufs.create();
            data.writeNbt(Config.getInstance().toNbtCompound());
            ServerPlayNetworking.send(handler.player, WaystonePacketHandler.WAYSTONE_CONFIG_UPDATE, data);

            FabricWaystones.WAYSTONE_STORAGE.sendToPlayer(handler.player);
            FabricWaystones.WAYSTONE_STORAGE.sendCompatData(handler.player);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(WaystonesWorldgen::registerVanillaVillageWorldgen);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ((PlayerEntityMixinAccess) newPlayer).syncData());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("fwaystones")
            .then(CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    Config.getInstance().loadConfig();
                    PacketByteBuf data = PacketByteBufs.create();
                    data.writeNbt(Config.getInstance().toNbtCompound());
                    for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                        ServerPlayNetworking.send(player, WaystonePacketHandler.WAYSTONE_CONFIG_UPDATE, data);
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3has successfully reloaded!"), false);
                    }
                    return 1;
                })
            )
            .then(CommandManager.literal("delete")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null || FabricWaystones.WAYSTONE_STORAGE == null) {
                        return 1;
                    }
                    var dimension = Utils.getDimensionName(player.world);
                    FabricWaystones.WAYSTONE_STORAGE.removeWorldWaystones(dimension);
                    player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3Removed all waystones from " + dimension + "!"), false);
                    return 1;
                })
            )
            .then(CommandManager.literal("display")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    Config.getInstance().print(player);
                    return 1;
                })
            )
            .then(CommandManager.literal("forget_all")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    ((PlayerEntityMixinAccess) player).forgetAllWaystones();
                    player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten!"), false);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                        if (target == null) {
                            return 1;
                        }
                        ((PlayerEntityMixinAccess) target).forgetAllWaystones();
                        player.sendMessage(Text.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten for " + target.getName() + "!"), false);
                        return 1;
                    })
                )
            )
        ));
    }

}