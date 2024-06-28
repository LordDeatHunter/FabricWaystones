package wraith.fwaystones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.packets.client.SyncPlayerPacket;
import wraith.fwaystones.packets.client.VoidRevivePacket;
import wraith.fwaystones.packets.client.WaystonePacket;
import wraith.fwaystones.registry.CustomBlockEntityRendererRegistry;
import wraith.fwaystones.registry.CustomScreenRegistry;
import wraith.fwaystones.registry.ItemRegistry;
import wraith.fwaystones.registry.WaystonesModelProviderRegistry;
import wraith.fwaystones.packets.WaystonePacketHandler;
import wraith.fwaystones.util.WaystoneStorage;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CustomBlockEntityRendererRegistry.RegisterBlockEntityRenderers();
        CustomScreenRegistry.registerScreens();
        WaystonesModelProviderRegistry.register();
        registerPacketHandlers();
        registerEvents();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(WaystonePacket.PACKET_ID, WaystonePacket.getClientPlayHandler());
        ClientPlayNetworking.registerGlobalReceiver(SyncPlayerPacket.PACKET_ID, SyncPlayerPacket.getClientPlayHandler());
        ClientPlayNetworking.registerGlobalReceiver(VoidRevivePacket.PACKET_ID, VoidRevivePacket.getClientPlayHandler());
    }

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                FabricWaystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                FabricWaystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            FabricWaystones.WAYSTONE_STORAGE.saveWaystones(false);
            FabricWaystones.WAYSTONE_STORAGE = null;
        });
    }

}
