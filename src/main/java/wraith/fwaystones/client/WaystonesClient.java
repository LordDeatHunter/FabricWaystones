package wraith.fwaystones.client;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.WaystoneEvents;
import wraith.fwaystones.api.client.MossColorProvidersRegistry;
import wraith.fwaystones.api.core.*;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.block.WaystoneBlockEntityRenderer;
import wraith.fwaystones.client.models.DynamicModelUtils;
import wraith.fwaystones.client.models.MultiBranchUnbakedModel;
import wraith.fwaystones.client.models.VariantUnbakedModel;
import wraith.fwaystones.integration.accessories.AccessoriesClientCompat;
import wraith.fwaystones.api.WaystoneInteractionEvents;
import wraith.fwaystones.integration.xaeros.XaerosMinimapWaypointMaker;
import wraith.fwaystones.item.components.TextUtils;
import wraith.fwaystones.item.components.WaystoneTyped;
import wraith.fwaystones.mixin.client.ModelPredicateProviderRegistryAccessor;
import wraith.fwaystones.item.render.WaystoneCompassItemRenderer;
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
import java.util.function.Consumer;

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

        BuiltinItemRendererRegistry.INSTANCE.register(WaystoneItems.WAYSTONE_COMPASS, new WaystoneCompassItemRenderer());
        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(FabricWaystones.id("item/waystone_compass_base")));

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

        ModelLoadingPlugin.register(ctx -> {
            var validItemModels = new LinkedHashMap<Identifier, Identifier>();

            for (var typeId : WaystoneTypes.getTypeIds()) {
                var modelId = typeId.withPrefixedPath("item/");

                ctx.addModels(modelId);

                validItemModels.put(modelId, typeId);
            }

            var validBlockModels = new LinkedHashMap<Identifier, Triple<Boolean, Boolean, WaystoneModelKey>>();

            for (var blockPosition : WaystoneBlock.HALF.getValues()) {
                for (var activeState : WaystoneBlock.ACTIVE.getValues()) {
                    for (var mossyState : WaystoneBlock.MOSSY.getValues()) {
                        Consumer<Identifier> modelCreator = mossTypeId -> {
                            for (var type : WaystoneTypes.getTypes()) {
                                var typeId = type.getId();

                                var isUpper = blockPosition.equals(DoubleBlockHalf.UPPER);

                                var modelId = createId(typeId, isUpper, activeState, mossTypeId);

                                var key = new WaystoneModelKey(typeId, mossTypeId);

                                validBlockModels.put(modelId, Triple.of(isUpper, activeState, key));

                                ctx.addModels(modelId);
                            }
                        };

                        if (mossyState) {
                            for (var mossTypeId : MossTypes.getTypeIds()) {
                                modelCreator.accept(mossTypeId);
                            }
                        } else {
                            modelCreator.accept(MossTypes.NO_MOSS_ID);
                        }
                    }
                }
            }

            ctx.resolveModel().register(context -> {
                if (validBlockModels.containsKey(context.id())) {
                    var data = validBlockModels.get(context.id());

                    var mossType = MossTypes.getType(data.getRight().mossType());
                    var waystoneType = WaystoneTypes.getType(data.getRight().type());

                    return createWaystoneModel(data.getLeft(), data.getMiddle(), mossType, waystoneType);
                }

                return null;
            });

            ctx.registerBlockStateResolver(WaystoneBlocks.WAYSTONE, stateCtx -> {
                for (var state : stateCtx.block().getStateManager().getStates()) {
                    var blockPosition = state.get(WaystoneBlock.HALF);
                    var activeState = state.get(WaystoneBlock.ACTIVE);
                    var mossyState = state.get(WaystoneBlock.MOSSY);
                    var direction = state.get(WaystoneBlock.FACING);

                    var yAxisRotation = direction.getOpposite().asRotation();

                    SequencedMap<WaystoneModelKey, UnbakedModel> typeToModel = new LinkedHashMap<>();

                    Consumer<@Nullable MossType> modelCreator = mossType -> {
                        for (var type : WaystoneTypes.getTypes()) {
                            var typeId = type.getId();

                            var isUpper = blockPosition.equals(DoubleBlockHalf.UPPER);

                            var mossId = mossType != null ? mossType.getId() : MossTypes.NO_MOSS_ID;

                            var modelId = createId(typeId, isUpper, activeState, mossId);

                            var model = (yAxisRotation == 0)
                                    ? new DelegatingUnbakedModel(modelId)
                                    : new VariantUnbakedModel(modelId, Math.round(yAxisRotation));

                            typeToModel.put(new WaystoneModelKey(typeId, mossId), model);
                        }
                    };

                    if (mossyState) {
                        for (var mossType : MossTypes.getTypes()) {
                            modelCreator.accept(mossType);
                        }
                    } else {
                        modelCreator.accept(null);
                    }

                    stateCtx.setModel(state, new MultiBranchUnbakedModel<>(typeToModel, (world, state1, pos) -> {
                        var blockEntity = WaystoneBlockEntity.getBlockEntity(world, pos, state);

                        if (blockEntity == null) return null;
                        var mossType = blockEntity.getMossType(state);

                        var typeId = blockEntity.getWaystoneTypeIdSafe();
                        var mossID = mossType != null ? mossType.getId() : MossTypes.NO_MOSS_ID;

                        return new WaystoneModelKey(typeId, mossID);
                    }));
                }
            });

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
                } else if(validItemModels.containsKey(context.id())) {
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
        });
    }

    public static Identifier createId(Identifier waystoneTypeId, boolean isUpper, boolean isActive, Identifier mossTypeId) {
        return waystoneTypeId.withPath(typeName -> {
            return "block/" + typeName + "waystone_" + (isUpper ? "top" : "bottom") + "_" + (isActive ? "active" : "inactive") + (!mossTypeId.equals(MossTypes.NO_MOSS_ID ) ? "_moss_" + mossTypeId.getPath() : "");
        });
    }

    public static JsonUnbakedModel createWaystoneModel(boolean isUpper, boolean isActive, @Nullable MossType mossType, WaystoneType waystoneType) {
        var mossy = mossType != null;

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
