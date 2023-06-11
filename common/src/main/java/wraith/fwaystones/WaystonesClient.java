package wraith.fwaystones;

import net.minecraft.client.Minecraft;
import wraith.fwaystones.registry.ModelProviderRegister;
import wraith.fwaystones.registry.ScreenRegister;
import wraith.fwaystones.util.EventManager;
import wraith.fwaystones.util.PacketHandler;

public class WaystonesClient {
	public static void init(Minecraft minecraft) {
		minecraft.submit(()->{
			ScreenRegister.register();
			ModelProviderRegister.register();
			PacketHandler.registerS2CListeners();
			EventManager.registerClient();
		});
	}
}