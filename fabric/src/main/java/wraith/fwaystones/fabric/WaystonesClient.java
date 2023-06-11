package wraith.fwaystones.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import wraith.fwaystones.registry.BlockEntityRendererRegister;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		wraith.fwaystones.WaystonesClient.init(Minecraft.getInstance());
		BlockEntityRendererRegister.register(BlockEntityRenderers::register);
	}
}
