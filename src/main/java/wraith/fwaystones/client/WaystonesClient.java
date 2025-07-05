package wraith.fwaystones.client;

import com.google.common.reflect.Reflection;
import com.google.gson.GsonBuilder;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.core.*;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.client.models.*;
import wraith.fwaystones.integration.accessories.AccessoriesClientCompat;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.item.components.WaystoneTyped;
import wraith.fwaystones.mixin.client.ModelPredicateProviderRegistryAccessor;
import wraith.fwaystones.item.render.WaystoneCompassRenderer;
import wraith.fwaystones.networking.WaystoneNetworkHandler;
import wraith.fwaystones.client.registry.WaystoneScreens;
import wraith.fwaystones.networking.packets.c2s.AttemptTeleporterUse;
import wraith.fwaystones.particle.RuneParticleEffect;
import wraith.fwaystones.registry.*;
import wraith.fwaystones.client.registry.WaystoneModelProviders;

import java.util.*;

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
                    return stack.contains(WaystoneDataComponents.TELEPORTER) || stack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEMS);
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

        ColorProviderRegistry.BLOCK.register(WaystoneBlockQuadEmission.COLOR_PROVIDER, WaystoneBlocks.WAYSTONE, WaystoneBlocks.WAYSTONE_SMALL);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), WaystoneBlocks.WAYSTONE, WaystoneBlocks.WAYSTONE_SMALL);

        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> {
                if (tintIndex == 1) {
                    var data = stack.getOrDefault(WaystoneDataComponents.DATA_HOLDER, null);
                    var type = stack.getOrDefault(WaystoneDataComponents.WAYSTONE_TYPE, null);
                    if (type == null) return Color.ofHsv(((System.currentTimeMillis() + stack.hashCode()) % 5000) / 5000f, 1, 1).rgb();
                    if (data == null) return type.getType().defaultRuneColor();
                    return data.data().color();
                }
                return -1;
            },
            WaystoneItems.WAYSTONE
        );

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

        ParticleFactoryRegistry.getInstance().register(WaystoneParticles.RUNE, RuneParticleEffect.Factory::new);

        register(FabricWaystones.id("waystone_type"), (stack, world, entity, seed) -> {
            var types = WaystoneTypes.getTypeIds();
            var id = stack.getOrDefault(WaystoneDataComponents.WAYSTONE_TYPE, new WaystoneTyped(types.get((int) ((System.currentTimeMillis() / (1000 + (stack.hashCode() % 100)) + stack.hashCode()) % types.size())))).id();
            if (types.contains(id)) return types.indexOf(id) + 1;
            return 1;
        });

        WaystoneCompassRenderer.init();

        ModelLoadingPlugin.register(ctx -> {
            var validItemModels = new LinkedHashMap<Identifier, Identifier>();

            for (var typeId : WaystoneTypes.getTypeIds()) {
                var modelId = typeId.withPath(s -> "item/" + s + "_waystone");

                ctx.addModels(modelId);

                validItemModels.put(modelId, typeId);
            }

            ctx.resolveModel().register(context -> {
                var gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
                var runes = FabricWaystones.id("item/waystone_runes");

                var id = context.id();

                if (id.equals(FabricWaystones.id("item/waystone")) || id.equals(FabricWaystones.id("item/waystone_small")) || id.equals(FabricWaystones.id("item/waystone_mini"))) {
                    var obj = DynamicModelUtils.createOverridenItemModel(
                        List.of(FabricWaystones.id("item/stone_waystone"), runes),
                        WaystoneTypes.getTypeIds().stream().map(typeId -> typeId.withPath(s -> "item/" + s + "_waystone")),
                        FabricWaystones.id("waystone_type")
                    );

                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                        FabricWaystones.LOGGER.info(gsonBuilder.toJson(obj));
                    }

                    return JsonUnbakedModel.deserialize(obj.toString());
                } else if (validItemModels.containsKey(id)) {
                    var typeId = validItemModels.get(context.id());
                    var type = WaystoneTypes.getType(typeId);

                    if (type == null) {
                        throw new IllegalStateException("Unable to get the required WaystoneType for getting the texture!");
                    }

                    var obj = DynamicModelUtils.createItemModel(List.of(type.itemTexture(), runes));

                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                        FabricWaystones.LOGGER.info(gsonBuilder.toJson(obj));
                    }

                    return JsonUnbakedModel.deserialize(obj.toString());
                }

                return null;
            });

            var bigModel = FabricWaystones.id("block/waystone_big");
            var bigMultiModel = FabricWaystones.id("block/multi_waystone_big");

            var smallModel = FabricWaystones.id("block/waystone_small");
            var smallMultiModel = FabricWaystones.id("block/multi_waystone_small");

            var plateModel = FabricWaystones.id("block/waystone_plate");
            var plateMultiModel = FabricWaystones.id("block/multi_waystone_plate");

            ctx.resolveModel().register(context -> {
                UnbakedModel possibleModel;

                if (context.id().equals(bigMultiModel)) {
                    possibleModel = context.getOrLoadModel(bigModel);
                } else if (context.id().equals(smallMultiModel)) {
                    possibleModel = context.getOrLoadModel(smallModel);
                } else if (context.id().equals(plateMultiModel)) {
                    possibleModel = context.getOrLoadModel(plateModel);
                } else {
                    return null;
                }

                return new CustomDelegatingUnbakedModel<>(possibleModel, WaystoneBlock::getEntity, WaystoneBlockQuadEmission.INSTANCE);
            });

            ctx.registerBlockStateResolver(WaystoneBlocks.WAYSTONE, stateCtx -> {
                for (var state : stateCtx.block().getStateManager().getStates()) {
                    var modelId = stateCtx.getOrLoadModel(bigMultiModel);

                    stateCtx.setModel(state, modelId);
                }
            });

            ctx.registerBlockStateResolver(WaystoneBlocks.WAYSTONE_SMALL, stateCtx -> {
                for (var state : stateCtx.block().getStateManager().getStates()) {
                    var modelId = stateCtx.getOrLoadModel(smallMultiModel);

                    stateCtx.setModel(state, modelId);
                }
            });

            ctx.registerBlockStateResolver(WaystoneBlocks.WAYSTONE_MINI, stateCtx -> {
                for (var state : stateCtx.block().getStateManager().getStates()) {
                    var modelId = stateCtx.getOrLoadModel(plateMultiModel);

                    stateCtx.setModel(state, modelId);
                }
            });
        });
    }

    public static void reloadPos(World world, BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (world == client.world) client.worldRenderer.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static void register(Identifier id, ModelPredicateProvider provider) {
        var providers = ModelPredicateProviderRegistryAccessor.fwaystones$GLOBAL();

        if (providers.containsKey(id)) throw new IllegalStateException("A provider with the same id was registered! Id: " + id);

        providers.put(id, provider);
    }
}
