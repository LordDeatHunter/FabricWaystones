package wraith.fwaystones.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import wraith.fwaystones.WaystonesClient;

@Environment(EnvType.CLIENT)
public class FabricWaystonesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WaystonesClient.registerBlockEntityRenderers(BlockEntityRenderers::register);
    }
}
