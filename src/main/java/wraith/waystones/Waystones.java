package wraith.waystones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import wraith.waystones.registries.*;

public class Waystones implements ModInitializer {

    public static final String MOD_ID = "waystones";
    public static WaystoneDatabase WAYSTONE_DATABASE;

    @Override
    public void onInitialize() {
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.registerBlockEntities();
        ItemRegistry.registerItems();
        CustomScreenHandlerRegistry.registerScreenHandlers();
        registerEvents();
        registerPacketHandlers();
    }


    private void registerPacketHandlers() {
        ServerSidePacketRegistry.INSTANCE.register(new Identifier(MOD_ID, "rename_waystone"), (packetContext, attachedData) -> {
            CompoundTag tag = attachedData.readCompoundTag();
            String oldName = tag.getString("old_name");
            String newName = tag.getString("new_name");
            if (WAYSTONE_DATABASE.containsWaystone(oldName)) {
                WAYSTONE_DATABASE.renameWaystone(oldName, newName);
            }
        });
        ServerSidePacketRegistry.INSTANCE.register(new Identifier(MOD_ID, "teleport_player"), (packetContext, attachedData) -> {
            CompoundTag tag = attachedData.readCompoundTag();
            teleportPlayer(packetContext.getPlayer(), tag);
        });
    }

    public static void teleportPlayer(PlayerEntity player, CompoundTag tag) {
        String world = tag.getString("WorldName");
        if (WAYSTONE_DATABASE.getWorld(world) == null) {
            return;
        }
        int[] coords = tag.getIntArray("Coordinates");
        ((ServerPlayerEntity)player).teleport(WAYSTONE_DATABASE.getWorld(world), coords[0], coords[1], coords[2], player.yaw, player.pitch);
    }

    public void registerEvents() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            WAYSTONE_DATABASE = new WaystoneDatabase(server);
            WAYSTONE_DATABASE.loadOrSaveWaystones(false);
        });
    }

}
