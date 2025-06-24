package wraith.fwaystones.client;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.WaystoneTypes;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.client.models.QuadEmission;
import wraith.fwaystones.registry.WaystoneBlocks;

import java.util.function.Supplier;

public class WaystoneBlockQuadEmission implements QuadEmission<WaystoneBlockEntity> {

    public static final WaystoneBlockQuadEmission INSTANCE = new WaystoneBlockQuadEmission();

    private WaystoneBlockQuadEmission(){}

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, WaystoneBlockEntity blockEntity, BakedModel model, Supplier<Random> random, RenderContext ctx) {
        var matrix = new Matrix4f();

        var direction = state.get(WaystoneBlock.FACING);

        if (direction.getAxis().equals(Direction.Axis.Z)) {
            direction = direction.getOpposite();
        }

        matrix.rotateAround(RotationAxis.POSITIVE_Y.rotationDegrees(direction.asRotation()), 0.5f, 0, 0.5f);

        ctx.pushTransform(quad -> {
            var vec = new Vector3f();

            for (int i = 0; i < 4; i++) {
                quad.copyPos(i, vec);

                vec.mulPosition(matrix);

                quad.pos(i, vec);
            }

            return true;
        });

        var atlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        {
            var type = blockEntity.getWaystoneType();

            var baseSprite = atlas.getSprite(WaystoneTypes.STONE_TYPE.blockTexture());
            var sprite = atlas.getSprite(type.blockTexture());

            var uOffset = sprite.getMinU() - baseSprite.getMinU();
            var vOffset = sprite.getMinV() - baseSprite.getMinV();

            ctx.pushTransform(quad -> {
                for (int i = 0; i < 4; i++) {
                    var currentU = quad.u(i);
                    var currentV = quad.v(i);

                    quad.uv(i, currentU + uOffset, currentV + vOffset);
                }

                return true;
            });

            model.emitBlockQuads(blockView, state, pos, random, ctx);

            ctx.popTransform();
        }

        {
            if (blockEntity.isActive()) {
                var runeId = FabricWaystones.id("block/waystone/waystone_runes");

                var baseSprite = atlas.getSprite(WaystoneTypes.STONE_TYPE.blockTexture());
                var sprite = atlas.getSprite(runeId);

                var uOffset = sprite.getMinU() - baseSprite.getMinU();
                var vOffset = sprite.getMinV() - baseSprite.getMinV();

                var color = 0xFF000000 | ColorProviderRegistry.BLOCK.get(WaystoneBlocks.WAYSTONE).getColor(state, blockView, pos, 1);

                ctx.pushTransform(quad -> {
                    for (int i = 0; i < 4; i++) {
                        var currentU = quad.u(i);
                        var currentV = quad.v(i);

                        quad.uv(i, currentU + uOffset, currentV + vOffset);

                        quad.color(i, color);
                    }

                    return true;
                });

                model.emitBlockQuads(blockView, state, pos, random, ctx);

                ctx.popTransform();
            }
        }

        {
            var mossType = blockEntity.getMossType();

            if (mossType != null) {
                var baseSprite = atlas.getSprite(WaystoneTypes.STONE_TYPE.blockTexture());
                var sprite = atlas.getSprite(mossType.blockTexture());

                var uOffset = sprite.getMinU() - baseSprite.getMinU();
                var vOffset = sprite.getMinV() - baseSprite.getMinV();

                ctx.pushTransform(quad -> {
                    for (int i = 0; i < 4; i++) {
                        var currentU = quad.u(i);
                        var currentV = quad.v(i);

                        quad.uv(i, currentU + uOffset, currentV + vOffset);
                    }

                    return true;
                });

                model.emitBlockQuads(blockView, state, pos, random, ctx);

                ctx.popTransform();
            }
        }

        ctx.popTransform();
    }

    @Override
    public Sprite getParticleSprite(BlockRenderView world, BlockPos pos, BlockState state, WaystoneBlockEntity blockEntity) {
        var modelManager = MinecraftClient.getInstance().getBakedModelManager();
        var atlas = modelManager.getAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        var type = blockEntity.getWaystoneType();
        var mossType = blockEntity.getMossType();

        if (mossType != null) return atlas.getSprite(mossType.blockTexture());

        var entries = type.materialBlockTag();

        if (entries.size() >= 1) {
            return modelManager.getBlockModels()
                .getModel(entries.get(0).value().getDefaultState())
                .getParticleSprite();
        }

        return atlas.getSprite(type.blockTexture());
    }
}
