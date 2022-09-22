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
import wraith.fwaystones.registry.CustomBlockEntityRendererRegistry;
import wraith.fwaystones.registry.CustomScreenRegistry;
import wraith.fwaystones.registry.ItemRegistry;
import wraith.fwaystones.registry.WaystonesModelProviderRegistry;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.util.WaystonePacketHandler;
import wraith.fwaystones.util.WaystoneStorage;

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
                if (FabricWaystones.WAYSTONE_STORAGE == null) {
                    FabricWaystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
                }
                FabricWaystones.WAYSTONE_STORAGE.fromTag(nbt);

                if (client.player == null) {
                    return;
                }
                HashSet<String> toForget = new HashSet<>();
                for (String hash : ((PlayerEntityMixinAccess) client.player).getDiscoveredWaystones()) {
                    if (!FabricWaystones.WAYSTONE_STORAGE.containsHash(hash)) {
                        toForget.add(hash);
                    }
                }
                ((PlayerEntityMixinAccess) client.player).forgetWaystones(toForget);

                if (client.player.currentScreenHandler instanceof UniversalWaystoneScreenHandler) {
                    ((UniversalWaystoneScreenHandler) client.player.currentScreenHandler).updateWaystones(client.player);
                }
            });
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
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                FabricWaystones.WAYSTONE_STORAGE = new WaystoneStorage(null);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (FabricWaystones.WAYSTONE_STORAGE == null) {
                FabricWaystones.LOGGER.error("The Waystone storage is null. This is likely caused by a crash.");
                return;
            }
            FabricWaystones.WAYSTONE_STORAGE.loadOrSaveWaystones(true);
            FabricWaystones.WAYSTONE_STORAGE = null;
        });
    }

}
