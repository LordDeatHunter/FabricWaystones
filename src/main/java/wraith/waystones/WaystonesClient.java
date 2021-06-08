package wraith.waystones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import wraith.waystones.registries.BlockEntityRendererRegistry;
import wraith.waystones.registries.CustomScreenRegistry;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {

    public static ClientWaystoneStorage WAYSTONE_STORAGE;

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.RegisterBlockEntityRenderers();
        CustomScreenRegistry.registerScreens();
        registerPacketHandlers();
        registerEvents();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_packet"), (client, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            if (Waystones.WAYSTONE_STORAGE != null) {
                client.getServer().execute(() -> Waystones.WAYSTONE_STORAGE.fromTag(tag));
            }
            client.execute(() -> WAYSTONE_STORAGE.fromTag(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_config_update"), (client, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            client.execute(() -> Config.getInstance().loadConfig(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("sync_player"), (client, networkHandler, data, sender) -> {
            CompoundTag tag = data.readCompoundTag();
            client.execute(() -> {
                if (client.player != null) {
                    ((PlayerEntityMixinAccess) client.player).fromTagW(tag);
                }
            });
        });
    }

    public void registerEvents() {
        ClientLoginConnectionEvents.INIT.register((handler, client) -> WAYSTONE_STORAGE = new ClientWaystoneStorage());
    }

}
