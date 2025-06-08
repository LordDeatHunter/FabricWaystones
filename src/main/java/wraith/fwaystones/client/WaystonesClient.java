package wraith.fwaystones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.BlockItem;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.integration.accessories.AccessoriesClientCompat;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.item.components.TooltipUtils;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.client.registry.WaystoneScreens;
import wraith.fwaystones.networking.packets.c2s.AttemptTeleporterUse;
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.client.registry.WaystoneModelProviders;
import wraith.fwaystones.registry.WaystoneDataComponents;

@Environment(EnvType.CLIENT)
public class WaystonesClient implements ClientModInitializer {

    public static final KeyBinding USE_TELEPORTER = new KeyBinding("fwaystones.key.use_teleporter", GLFW.GLFW_KEY_U, "fwaystones.key.category.main");

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(WaystoneBlockEntities.WAYSTONE_BLOCK_ENTITY, WaystoneBlockEntityRenderer::new);
        WaystoneScreens.register();
        WaystoneModelProviders.register();
        WaystoneNetworkHandler.initClient();

        KeyBindingHelper.registerKeyBinding(USE_TELEPORTER);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (USE_TELEPORTER.wasPressed()){
                var ref = WaystoneInteractionEvents.LOCATE_EQUIPMENT.invoker().getStack(client.player, stack -> {
                    return stack.contains(WaystoneDataComponents.TELEPORTER) || stack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEM);
                });

                if (ref != null) {
                    WaystoneNetworkHandler.CHANNEL.clientHandle().send(new AttemptTeleporterUse());
                }
            }
        });

        if (FabricLoader.getInstance().isModLoaded("accessories")) {
            AccessoriesClientCompat.init();
        }

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WaystoneBlock) {
                var cooldownAmount = FabricWaystones.CONFIG.teleportCooldowns.usedWaystone();

                if(cooldownAmount > 0) {
                    lines.add(TooltipUtils.translationWithArg("cool_down.tooltip", String.valueOf(cooldownAmount / 20)));
                }
            }
        });
    }
}
