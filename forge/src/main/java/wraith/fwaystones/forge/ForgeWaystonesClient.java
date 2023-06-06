package wraith.fwaystones.forge;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import wraith.fwaystones.WaystonesClient;

import java.util.function.Consumer;

public class ForgeWaystonesClient {
    public static void init(IEventBus eventBus) {
        eventBus.addListener((Consumer<EntityRenderersEvent.RegisterRenderers>) event -> {
            WaystonesClient.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
        });


        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> {
            WaystonesClient.onInitialize(minecraft);
        });


    }
}
