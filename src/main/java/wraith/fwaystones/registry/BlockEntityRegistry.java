package wraith.fwaystones.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.block.WaystoneBlockEntity;
import wraith.fwaystones.util.Utils;

public final class BlockEntityRegistry {

    public static final BlockEntityType<WaystoneBlockEntity> WAYSTONE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(WaystoneBlockEntity::new,
            BlockRegistry.WAYSTONE,
            BlockRegistry.DESERT_WAYSTONE,
            BlockRegistry.STONE_BRICK_WAYSTONE,
            BlockRegistry.RED_DESERT_WAYSTONE,
            BlockRegistry.RED_NETHER_BRICK_WAYSTONE,
            BlockRegistry.NETHER_BRICK_WAYSTONE,
            BlockRegistry.ENDSTONE_BRICK_WAYSTONE,
            BlockRegistry.DEEPSLATE_BRICK_WAYSTONE,
            BlockRegistry.BLACKSTONE_BRICK_WAYSTONE
    ).build();

    public static void registerBlockEntities() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Utils.ID("waystone"), WAYSTONE_BLOCK_ENTITY);
    }

}
