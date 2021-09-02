package wraith.waystones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import wraith.waystones.util.Config;
import wraith.waystones.util.Utils;
import wraith.waystones.Waystones;
import wraith.waystones.interfaces.PlayerEntityMixinAccess;
import wraith.waystones.registries.CustomBlockEntityRendererRegistry;
import wraith.waystones.registries.CustomScreenRegistry;
import wraith.waystones.registries.WaystonesModelProviderRegistry;
import wraith.waystones.screens.UniversalWaystoneScreenHandler;

import java.util.HashSet;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {

    public static ClientWaystoneStorage WAYSTONE_STORAGE;

    @Override
    public void onInitializeClient() {
        CustomBlockEntityRendererRegistry.RegisterBlockEntityRenderers();
        CustomScreenRegistry.registerScreens();
        WaystonesModelProviderRegistry.register();
        registerPacketHandlers();
        registerEvents();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_packet"), (client, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            if (Waystones.WAYSTONE_STORAGE != null && client.getServer() != null) {
                client.getServer().execute(() -> Waystones.WAYSTONE_STORAGE.fromTag(tag));
            }
            client.execute(() -> {
                WAYSTONE_STORAGE.fromTag(tag);

                if (client.player == null) {
                    return;
                }
                HashSet<String> toForget = new HashSet<>();
                for (String hash : ((PlayerEntityMixinAccess)client.player).getDiscoveredWaystones()) {
                    if (!WAYSTONE_STORAGE.containsWaystone(hash)) {
                        toForget.add(hash);
                    }
                }
                ((PlayerEntityMixinAccess)client.player).forgetWaystones(toForget);

                if (client.player.currentScreenHandler instanceof UniversalWaystoneScreenHandler) {
                    ((UniversalWaystoneScreenHandler)client.player.currentScreenHandler).updateWaystones(client.player);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_config_update"), (client, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            client.execute(() -> Config.getInstance().loadConfig(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("sync_player"), (client, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            client.execute(() -> {
                if (client.player != null) {
                    ((PlayerEntityMixinAccess) client.player).fromTagW(tag);
                }
            });
        });
    }

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WAYSTONE_STORAGE = new ClientWaystoneStorage());
    }

}
