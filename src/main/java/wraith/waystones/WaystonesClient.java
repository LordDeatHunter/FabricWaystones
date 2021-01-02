package wraith.waystones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import wraith.waystones.registries.BlockEntityRendererRegistry;
import wraith.waystones.registries.CustomScreenRegistry;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.RegisterBlockEntityRenderers();
        CustomScreenRegistry.registerScreens();
        registerPacketHandlers();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_packet"), (client, networkHandler, data, sender) -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(!(networkHandler.getConnection().isLocal())) {
                Waystones.WAYSTONE_DATABASE = new WaystoneDatabase(player == null ? null : player.getServer());
            }

            ListTag list = data.readCompoundTag().getList("Waystones", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag tag = list.getCompound(i);
                String name = tag.getString("Name");
                String world = tag.getString("World");
                String facing = tag.getString("Facing");
                int[] coords = tag.getIntArray("Coordinates");
                BlockPos pos = new BlockPos(coords[0], coords[1], coords[2]);
                Waystone waystone = new Waystone(name, pos, world, facing);
                Waystones.WAYSTONE_DATABASE.addWaystone(waystone);
                Waystones.WAYSTONE_DATABASE.discoverWaystone(player, name);
            }

        });
        ClientPlayNetworking.registerGlobalReceiver(Utils.ID("waystone_config_update"), (client, networkHandler, data, sender) -> {
            if (client.isInSingleplayer()) {
                return;
            }
            CompoundTag tag = data.readCompoundTag();
            String config = tag.getString("config");
            String recipe = tag.getString("recipe");
            Config.getInstance().loadConfig(Config.getJsonObject(config), Config.getJsonObject(recipe));
        });
    }

}
