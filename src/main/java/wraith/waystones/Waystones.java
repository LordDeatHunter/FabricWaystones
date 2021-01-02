package wraith.waystones;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import wraith.waystones.registries.BlockEntityRegistry;
import wraith.waystones.registries.BlockRegistry;
import wraith.waystones.registries.CustomScreenHandlerRegistry;
import wraith.waystones.registries.ItemRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class Waystones implements ModInitializer {

    public static final String MOD_ID = "waystones";
    public static WaystoneDatabase WAYSTONE_DATABASE;

    @Override
    public void onInitialize() {
        LogManager.getLogger().info("[Fabric-Waystones] is loading.");
        Config.getInstance().loadConfig();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.addItems();
        ItemRegistry.registerItems();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        registerEvents();
        registerPacketHandlers();
        LogManager.getLogger().info("[Fabric-Waystones] has successfully loaded.");
        LogManager.getLogger().info("[Fabric-Waystones] If you have any issues or questions, feel free to join our Discord: https://discord.gg/vMjzgS4.");
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("rename_waystone"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (tag == null) {
                return;
            }
            String oldName = tag.getString("old_name");
            String newName = tag.getString("new_name");
            if (WAYSTONE_DATABASE.containsWaystone(oldName)) {
                WAYSTONE_DATABASE.renameWaystone(oldName, newName);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("discover_waystones"), (server, player, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (tag == null) {
                return;
            }
            for (String key : tag.getKeys()) {
                Waystones.WAYSTONE_DATABASE.discoverWaystone(player, key);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("get_waystones"), (server, player, networkHandler, data, sender) -> WAYSTONE_DATABASE.sendToPlayer(player));
        ServerPlayNetworking.registerGlobalReceiver(Utils.ID("forget_waystone"), (server, player, networkHandler, data, sender) -> {
            String id = data.readCompoundTag().getString("id");
            if (Config.getInstance().canGlobalDiscover()) {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    WAYSTONE_DATABASE.forgetWaystone(p, id);
                }
            } else {
                WAYSTONE_DATABASE.forgetWaystone(player, id);
            }
        });
    }

    public static void teleportPlayer(PlayerEntity player, String world, String facing, BlockPos pos) {
        if (WAYSTONE_DATABASE.getWorld(world) == null) {
            return;
        }
        float x = 0;
        float z = 0;
        float yaw = player.yaw;
        switch (facing) {
            case "north":
                x = 0.5f;
                z = -0.5f;
                yaw = 0;
                break;
            case "south":
                x = 0.5f;
                z = 1.5f;
                yaw = 180;
                break;
            case "east":
                x = 1.5f;
                z = 0.5f;
                yaw = 90;
                break;
            case "west":
                x = -0.5f;
                z = 0.5f;
                yaw = 270;
                break;
        }

        final float fX = x;
        final float fZ = z;
        final float fYaw = yaw;
        final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        final List<StatusEffectInstance> effects = serverPlayer.getStatusEffects().stream().map(StatusEffectInstance::new).collect(Collectors.toList());
            
        Runnable r = () -> {
            serverPlayer.teleport(WAYSTONE_DATABASE.getWorld(world), pos.getX() + fX, pos.getY(), pos.getZ() + fZ, fYaw, 0);
            serverPlayer.onTeleportationDone();
            serverPlayer.addExperience(0);
            serverPlayer.clearStatusEffects();
                
                for (StatusEffectInstance effect : effects) {
                    serverPlayer.addStatusEffect(effect);
                }
                 
        };
        serverPlayer.getServer().execute(r);
    }

    public void registerEvents() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            WAYSTONE_DATABASE = new WaystoneDatabase(server);
            WAYSTONE_DATABASE.loadOrSaveWaystones(false);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("waystonesreload")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    Config.getInstance().loadConfig();
                    for (ServerPlayerEntity player : context.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
                        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
                        data.writeCompoundTag(Config.getInstance().toCompoundTag());
                        ServerPlayNetworking.send(player, Utils.ID("waystone_config_update"), data);
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        player.sendMessage(new LiteralText("§6[§eWaystones§6] §3has successfully reloaded!"), false);
                    }
                    return 1;
                })
        )
        );
    }

}
