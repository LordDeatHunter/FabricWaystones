package wraith.fwaystones.client;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.core.DataChangeType;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.integration.accessories.AccessoriesClientCompat;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import wraith.fwaystones.item.components.TextUtils;
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
            if (USE_TELEPORTER.wasPressed()) {
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

                if (cooldownAmount > 0) {
                    lines.add(TextUtils.translationWithArg("cool_down.tooltip", String.valueOf(cooldownAmount / 20)));
                }
            }
        });

        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            Reflection.initialize(XaerosMinimapWaypointMaker.class);
        }

        for (Block block : Registries.BLOCK) {
            if (block instanceof WaystoneBlock waystoneBlock) {
                ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    var color = waystoneBlock.type.defaultColor();
                    if (world == null || pos == null) return color;
                    var blockEntity = world.getBlockEntity(state.get(WaystoneBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos);
                    if (!(blockEntity instanceof WaystoneBlockEntity waystoneBlockEntity)) return color;
                    var data = waystoneBlockEntity.getData();
                    if (data == null) return color;
                    return data.color();
                }, block);
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());
            }
        }

        WaystoneEvents.ON_WAYSTONE_DATA_UPDATE.register((uuid, type) -> {
            if (type.equals(DataChangeType.COLOR)) {
                MinecraftClient client = MinecraftClient.getInstance();
                World world = client.world;
                if (world == null) return;
                var storage = WaystoneDataStorage.getStorage(world);
                var pos = storage.getPosition(uuid);
                if (pos != null) {
                    reloadPos(world, pos.blockPos());
                    reloadPos(world, pos.blockPos().up());
                }
            }
        });
    }

    public static void reloadPos(World world, BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (world == client.world) client.worldRenderer.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
}
