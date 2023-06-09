package wraith.fwaystones;

import net.minecraft.client.Minecraft;
import wraith.fwaystones.util.EventManager;
import wraith.fwaystones.util.PacketHandler;

public class WaystonesClient {
	public static void init(Minecraft minecraft) {
		minecraft.submit(()->{
			//ScreenReg.register();
			//ModelProviderReg.register();
			PacketHandler.registerS2CListeners();
			EventManager.registerClient();
		});
	}
}
