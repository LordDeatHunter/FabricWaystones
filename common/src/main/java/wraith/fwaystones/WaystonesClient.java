package wraith.fwaystones;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.registry.BlockEntityReg;
import wraith.fwaystones.util.EventManager;
import wraith.fwaystones.util.PacketHandler;

import java.util.function.BiConsumer;

public class WaystonesClient {
    public static void onInitialize(Minecraft minecraft) {
        minecraft.submit(()->{
            //MenuRegistry.registerScreenFactory(MenuReg.ABYSS_MENU.get(), AbyssScreen::new);
            //CustomScreenRegistry.registerScreens();
            //WaystonesModelProviderRegistry.register();
            PacketHandler.registerS2CListeners();
            EventManager.registerClient();
        });
    }
    public static void registerBlockEntityRenderers(BiConsumer<BlockEntityType<? extends BlockEntity>, BlockEntityRendererProvider> consumer) {
        consumer.accept(BlockEntityReg.WAYSTONE_BLOCK_ENTITY.get(), WaystoneBlockEntityRenderer::new);
    }
}
