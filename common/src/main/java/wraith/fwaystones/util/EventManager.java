package wraith.fwaystones.util;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;

public class EventManager {
	public static void registerClient(){
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player->{
			if (Waystones.WAYSTONE_STORAGE == null) {
				Waystones.WAYSTONE_STORAGE = new Storage(null);
			}
		});
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player->{
			if (Waystones.WAYSTONE_STORAGE == null) {
				Waystones.LOGGER.error("CLIENT : The Waystone storage is null. This is likely caused by a crash.");
				return;
			}
			Waystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
			Waystones.WAYSTONE_STORAGE = null;
		});
	}
	public static void registerServer(){
		LifecycleEvent.SERVER_STARTED.register((server)->{
			if (Waystones.WAYSTONE_STORAGE == null) {
				Waystones.WAYSTONE_STORAGE = new Storage(server);
			}
		});
		LifecycleEvent.SERVER_STOPPED.register((server)->{
			if (Waystones.WAYSTONE_STORAGE == null) {
				if (server.isDedicatedServer())
					Waystones.LOGGER.error("SERVER : The Waystone storage is null. This is likely caused by a crash.");
				return;
			}
			Waystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
			Waystones.WAYSTONE_STORAGE = null;
		});
		PlayerEvent.PLAYER_JOIN.register(player -> Waystones.WAYSTONE_STORAGE.sendToPlayer(player));
		LifecycleEvent.SERVER_STARTING.register(Worldgen::registerVanillaVillageWorldgen);
		/*TODO: ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> FabricWaystones.CONFIG.load());
		 * ConfigModel.reload()
		 * */
		PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd) -> ((PlayerEntityMixinAccess) newPlayer).syncData());

		CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal(Waystones.MOD_ID)
				.then(Commands.literal("delete")
						.requires(sources -> sources.hasPermission(1))
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayer();
							if (player == null || Waystones.WAYSTONE_STORAGE == null)
								return 1;
							var dimension = Utils.getDimensionName(player.level);
							Waystones.WAYSTONE_STORAGE.removeWorldWaystones(dimension);
							player.displayClientMessage(Component.literal("§6[§eFabric Waystones§6] §3Removed all waystones from " + dimension + "!"), false);
							return 1;
						})
				).then(Commands.literal("forget_all")
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayer();
							if (player == null) {
								return 1;
							}
							((PlayerEntityMixinAccess) player).forgetAllWaystones();
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
									((PlayerEntityMixinAccess) target).forgetAllWaystones();
									player.displayClientMessage(Component.literal("§6[§eFabric Waystones§6] §3All waystones have been forgotten for " + target.getName() + "!"), false);
									return 1;
								})
						)
				)

		));
	}
}
