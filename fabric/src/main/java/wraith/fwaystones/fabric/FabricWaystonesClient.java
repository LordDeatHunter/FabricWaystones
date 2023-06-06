package wraith.fwaystones.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import wraith.fwaystones.WaystonesClient;

@Environment(EnvType.CLIENT)
public class FabricWaystonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WaystonesClient.onInitialize(Minecraft.getInstance());
        WaystonesClient.registerBlockEntityRenderers(BlockEntityRenderers::register);
    }
}
