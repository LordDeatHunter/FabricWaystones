package wraith.waystones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import wraith.waystones.Waystones;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.registry.CustomBlockEntityRendererRegistry;
import wraith.waystones.registry.CustomScreenRegistry;
import wraith.waystones.registry.ItemRegistry;
import wraith.waystones.registry.WaystonesModelProviderRegistry;
import wraith.waystones.screen.UniversalWaystoneScreenHandler;
import wraith.waystones.util.Config;
import wraith.waystones.util.WaystonePacketHandler;
import wraith.waystones.util.WaystoneStorage;

import java.util.HashSet;

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
        ClientPlayNetworking.registerGlobalReceiver(WaystonePacketHandler.WAYSTONE_PACKET, (client, networkHandler, data, sender) -> {
            var nbt = data.readNbt();
            client.execute(() -> {
                if (Waystones.WAYSTONE_STORAGE == null) {
                    Waystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
                }
                Waystones.WAYSTONE_STORAGE.fromTag(nbt);

                if (client.player == null) {
                    return;
                }
                HashSet<String> toForget = new HashSet<>();
                for (String hash : ((PlayerEntityMixinAccess) client.player).getDiscoveredWaystones()) {
                    if (!Waystones.WAYSTONE_STORAGE.containsHash(hash)) {
                        toForget.add(hash);
                    }
                }
                ((PlayerEntityMixinAccess) client.player).forgetWaystones(toForget);

                if (client.player.currentScreenHandler instanceof UniversalWaystoneScreenHandler) {
                    ((UniversalWaystoneScreenHandler) client.player.currentScreenHandler).updateWaystones(client.player);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(WaystonePacketHandler.WAYSTONE_CONFIG_UPDATE, (client, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            client.execute(() -> Config.getInstance().loadConfig(tag));
        });
        ClientPlayNetworking.registerGlobalReceiver(WaystonePacketHandler.SYNC_PLAYER, (client, networkHandler, data, sender) -> {
            NbtCompound tag = data.readNbt();
            client.execute(() -> {
                if (client.player != null) {
                    ((PlayerEntityMixinAccess) client.player).fromTagW(tag);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(WaystonePacketHandler.VOID_REVIVE, (client, handler, packet, sender) -> {
            if (client.player == null) {
                return;
            }
            client.particleManager.addEmitter(client.player, ParticleTypes.TOTEM_OF_UNDYING, 30);
            handler.getWorld().playSound(client.player.getX(), client.player.getY(), client.player.getZ(), SoundEvents.ITEM_TOTEM_USE, client.player.getSoundCategory(), 1.0F, 1.0F, false);
            for (int i = 0; i < client.player.getInventory().size(); ++i) {
                ItemStack playerStack = client.player.getInventory().getStack(i);
                if (playerStack.getItem() == ItemRegistry.get("void_totem")) {
                    client.gameRenderer.showFloatingItem(playerStack);
                    break;
                }
            }
        });
    }

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (Waystones.WAYSTONE_STORAGE == null) {
                Waystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (Waystones.WAYSTONE_STORAGE == null) {
                Waystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            Waystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            Waystones.WAYSTONE_STORAGE = null;
        });
    }

}
