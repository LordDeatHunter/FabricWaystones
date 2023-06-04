package wraith.fwaystones.forge;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.WaystonesClient;

import java.util.function.Consumer;

public class ForgeWaystonesClient {
    public static void init(IEventBus eventBus) {
        eventBus.addListener((Consumer<EntityRenderersEvent.RegisterRenderers>) event -> {
            WaystonesClient.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
        });
    }
}
