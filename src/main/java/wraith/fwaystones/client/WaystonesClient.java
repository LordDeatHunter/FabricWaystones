package wraith.fwaystones.client;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.client.MossColorProvidersRegistry;
import wraith.fwaystones.api.core.*;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
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
import wraith.fwaystones.registry.WaystoneBlockEntities;
import wraith.fwaystones.client.registry.WaystoneModelProviders;
import wraith.fwaystones.registry.WaystoneBlocks;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.registry.WaystoneItems;
import wraith.fwaystones.registry.WaystoneParticles;

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
            if (block instanceof WaystoneBlock) {
                ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
                    if (world != null && pos != null) {
                        var blockEntity = WaystoneBlockEntity.getBlockEntity(world, pos, state);

                        if (blockEntity != null) {
                            if (tintIndex == 1) {
                                var data = blockEntity.getData();

                                var dataColor = (data != null ? data.color() : -1);
                                var tintColor = blockEntity.getWaystoneType().defaultRuneColor();

                                if (dataColor != -1 && dataColor != tintColor) {
                                    tintColor = dataColor;
                                }

                                return tintColor;
                            } else if (tintIndex == 2) {
                                var mossType = blockEntity.getMossType(state);

                                if (mossType != null) {
                                    var provider = MossColorProvidersRegistry.getProvider(mossType);

                                    if (provider != null) {
                                        return provider.getColor(state, world, pos, tintIndex);
                                    }
                                }
                            }
                        }
                    }

                    return -1;
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

        ParticleFactoryRegistry.getInstance().register(WaystoneParticles.RUNE, RuneParticleEffect.Factory::new);

        register(FabricWaystones.id("waystone_type"), (stack, world, entity, seed) -> {
            var id = stack.getOrDefault(WaystoneDataComponents.WAYSTONE_TYPE, WaystoneTyped.DEFAULT).id();

            var types = WaystoneTypes.getTypeIds();

            if (types.contains(id)) return types.indexOf(id) + 1;

            return 1;
        });

        WaystoneCompassRenderer.init();

        ModelLoadingPlugin.register(ctx -> {
            ctx.addModels(FabricWaystones.id("item/waystone_compass_base"));

            var validItemModels = new LinkedHashMap<Identifier, Identifier>();

            for (var typeId : WaystoneTypes.getTypeIds()) {
                var modelId = typeId.withPath(s -> "item/" + s + "_waystone");

                ctx.addModels(modelId);

                validItemModels.put(modelId, typeId);
            }

            ctx.resolveModel().register(context -> {
                if (context.id().equals(FabricWaystones.id("item/waystone"))) {
                    var obj = DynamicModelUtils.createOverridenItemModel(
                        List.of(FabricWaystones.id("item/stone_waystone")),
                        WaystoneTypes.getTypeIds().stream().map(id -> id.withPath(s -> "item/" + s + "_waystone")),
                        FabricWaystones.id("waystone_type")
                    );

                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                        FabricWaystones.LOGGER.info(obj);
                    }

                    return JsonUnbakedModel.deserialize(obj.toString());
                } else if (validItemModels.containsKey(context.id())) {
                    var typeId = validItemModels.get(context.id());
                    var type = WaystoneTypes.getType(typeId);

                    if (type == null) {
                        throw new IllegalStateException("Unable to get the required WaystoneType for getting the texture!");
                    }

                    var obj = DynamicModelUtils.createItemModel(List.of(type.itemTexture()));

                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                        FabricWaystones.LOGGER.info(obj);
                    }

                    return JsonUnbakedModel.deserialize(obj.toString());
                }

                return null;
            });

            var baseBottomModel = FabricWaystones.id("block/waystone_bottom");
            var baseTopModel = FabricWaystones.id("block/waystone_top");

            var multiBottomModel = FabricWaystones.id("block/multi_waystone_bottom");
            var multiTopModel = FabricWaystones.id("block/multi_waystone_top");

            ctx.resolveModel().register(context -> {
                UnbakedModel possibleModel;

                if (context.id().equals(multiBottomModel)) {
                    possibleModel = context.getOrLoadModel(baseBottomModel);
                } else if(context.id().equals(multiTopModel)) {
                    possibleModel = context.getOrLoadModel(baseTopModel);
                } else {
                    return null;
                }

                return new CustomDelegatingUnbakedModel<>(possibleModel, WaystoneBlockEntity::getBlockEntity, WaystoneBlockQuadEmission.INSTANCE);
            });

            ctx.registerBlockStateResolver(WaystoneBlocks.WAYSTONE, stateCtx -> {
                for (var state : stateCtx.block().getStateManager().getStates()) {
                    var blockPosition = state.get(WaystoneBlock.HALF);

                    stateCtx.setModel(state, stateCtx.getOrLoadModel(blockPosition.equals(DoubleBlockHalf.LOWER) ? multiBottomModel : multiTopModel));
                }
            });
        });
    }

    public static Identifier createId(Identifier waystoneTypeId, boolean isUpper, boolean isActive, Identifier mossTypeId) {
        return waystoneTypeId.withPath(typeName -> {
            return "block/" + typeName + "waystone_" + (isUpper ? "top" : "bottom") + "_" + (isActive ? "active" : "inactive") + (!mossTypeId.equals(MossTypes.EMPTY_ID) ? "_moss_" + mossTypeId.getPath() : "");
        });
    }

    public static JsonUnbakedModel createWaystoneModel(boolean isUpper, boolean isActive, MossType mossType, WaystoneType waystoneType) {
        var mossy = mossType != MossType.EMPTY;

        return JsonUnbakedModel.deserialize(
            DynamicModelUtils.createBlockModel(
                FabricWaystones.id("block/base_waystone_" + (isUpper ? "top" : "bottom") + (isActive ? "" : "_off") + (mossy ? "_mossy" : "")),
                waystoneType.particleTexture(),
                map -> {
                    map.put("body", waystoneType.blockTexture());
                    if (mossy) {
                        map.put("moss", mossType.blockTexture());
                    }
                }
            ).toString()
        );
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
