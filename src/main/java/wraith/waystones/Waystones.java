package wraith.waystones;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.registries.BlockRegistry;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

import java.util.UUID;

public class Waystones implements ModInitializer {

    public static final String MOD_ID = "waystones";
    public static WaystoneStorage WAYSTONE_STORAGE;
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        LogManager.getLogger().info("[Fabric-Waystones] is initializing.");
        Config.getInstance().loadConfig();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.registerItems();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        registerEvents();
        registerPacketHandlers();
        LogManager.getLogger().info("[Fabric-Waystones] has successfully been initialized.");
        LogManager.getLogger().info("[Fabric-Waystones] If you have any issues or questions, feel free to join our Discord: https://discord.gg/vMjzgS4.");
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("remove_waystone_owner"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            server.execute(() -> {
                if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                UUID owner = tag.getUuid("waystone_owner");
                if (WAYSTONE_STORAGE.containsHash(hash) && ((player.getUuid().equals(owner) && WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) || player.hasPermissionLevel(2))) {
                    WAYSTONE_STORAGE.setOwner(hash, null);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_gui_slot_click"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            server.execute(() -> {
                if (tag == null || !tag.contains("sync_id") || !tag.contains("clicked_slot")) {
                    return;
                }
                int syncId = tag.getInt("sync_id");
                int button = tag.getInt("clicked_slot");
                if (player.currentScreenHandler.syncId == syncId) {
                    player.currentScreenHandler.onButtonClick(player, button);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("rename_waystone"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (tag == null || !tag.contains("waystone_name") || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            String name = tag.getString("waystone_name");
            String hash = tag.getString("waystone_hash");
            UUID owner = tag.getUuid("waystone_owner");
            server.execute(() -> {
                if (WAYSTONE_STORAGE.containsHash(hash) && ((player.getUuid().equals(owner) && WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) || player.hasPermissionLevel(2))) {
                    WAYSTONE_STORAGE.renameWaystone(hash, name);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("forget_waystone"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();

            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }

            String hash = tag.getString("waystone_hash");
            server.execute(() -> {
                WaystoneBlockEntity waystone = WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone == null || waystone.isGlobal()) {
                    return;
                }
                ((PlayerEntityMixinAccess)player).forgetWaystone(hash);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("request_player_waystone_update"), (server, player, networkHandler, data, sender) -> server.execute(((PlayerEntityMixinAccess) player)::syncData));
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("toggle_global_waystone"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (tag == null || !tag.contains("waystone_hash") || !tag.contains("waystone_owner")) {
                return;
            }
            server.execute(() -> {
                String hash = tag.getString("waystone_hash");
                UUID owner = tag.getUuid("waystone_owner");
                if (WAYSTONE_STORAGE.containsHash(hash) && ((player.getUuid().equals(owner) && WAYSTONE_STORAGE.getWaystone(hash).getOwner().equals(owner)) || player.hasPermissionLevel(2))) {
                    WAYSTONE_STORAGE.toggleGlobal(hash);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("sync_player_from_client"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            server.execute(() -> ((PlayerEntityMixinAccess)player).fromTagW(tag));
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("teleport_to_waystone"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (tag == null || !tag.contains("waystone_hash")) {
                return;
            }
            server.execute(() -> {
                if (!tag.contains("waystone_hash")) {
                    return;
                }
                String hash = tag.getString("waystone_hash");
                boolean isAbyssWatcher = tag.contains("from_abyss_watcher") && tag.getBoolean("from_abyss_watcher");
                WaystoneBlockEntity waystone = WAYSTONE_STORAGE.getWaystone(hash);
                if (waystone == null) {
                    return;
                }
                if (waystone.getWorld() != null && waystone.getWorld().getBlockState(waystone.getPos()).getBlock() != BlockRegistry.WAYSTONE) {
                    WAYSTONE_STORAGE.removeWaystone(hash);
                    waystone.getWorld().removeBlockEntity(waystone.getPos());
                } else if (Utils.canTeleport(player, hash)) {
                    BlockPos playerPos = player.getBlockPos();
                    waystone.teleportPlayer(player, isAbyssWatcher);

                    player.world.playSound(player, playerPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
                    if (isAbyssWatcher) {
                        player.world.playSound(player, playerPos, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 1F, 1F);
                    }

                    BlockPos waystonePos = waystone.getPos();
                    if (!waystonePos.isWithinDistance(playerPos, 6)) {
                        player.world.playSound(player, waystonePos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
                    }
                }
            });
        });
    }


    public void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> WAYSTONE_STORAGE = new WaystoneStorage(server));
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
            WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            WAYSTONE_STORAGE = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
            data.writeCompoundTag(Config.getInstance().toCompoundTag());
            ServerPlayNetworking.send(handler.player, Utils.ID("waystone_config_update"), data);

            Waystones.WAYSTONE_STORAGE.sendToPlayer(handler.player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ((PlayerEntityMixinAccess)newPlayer).syncData());

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("waystones")
            .then(CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    Config.getInstance().loadConfig();
                    PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
                    data.writeCompoundTag(Config.getInstance().toCompoundTag());
                    for (ServerPlayerEntity player : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
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
