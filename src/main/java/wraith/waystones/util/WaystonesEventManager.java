package wraith.waystones.util;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import wraith.waystones.Waystones;
import wraith.waystones.access.PlayerEntityMixinAccess;

public class WaystonesEventManager {

    private WaystonesEventManager() {}

    public static void registerEvents() {

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (Waystones.WAYSTONE_STORAGE == null) {
                Waystones.WAYSTONE_STORAGE = new WaystoneStorage(server);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            if (Waystones.WAYSTONE_STORAGE == null) {
                if (server.isDedicated())
                    Waystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            Waystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            Waystones.WAYSTONE_STORAGE = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PacketByteBuf data = PacketByteBufs.create();
            data.writeNbt(Config.getInstance().toNbtCompound());
            ServerPlayNetworking.send(handler.player, Utils.ID("waystone_config_update"), data);

            Waystones.WAYSTONE_STORAGE.sendToPlayer(handler.player);
            Waystones.WAYSTONE_STORAGE.sendCompatData(handler.player);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(WaystonesWorldgen::registerVanillaVillageWorldgen);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ((PlayerEntityMixinAccess) newPlayer).syncData());

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("waystones")
                .then(CommandManager.literal("reload")
                        .requires(source -> source.hasPermissionLevel(1))
                        .executes(context -> {
                            Config.getInstance().loadConfig();
                            PacketByteBuf data = PacketByteBufs.create();
                            data.writeNbt(Config.getInstance().toNbtCompound());
                            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                ServerPlayNetworking.send(player, Utils.ID("waystone_config_update"), data);
                            }
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                player.sendMessage(new LiteralText("§6[§eWaystones§6] §3has successfully reloaded!"), false);
                            }
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
        ));
    }

}