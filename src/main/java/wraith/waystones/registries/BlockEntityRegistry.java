package wraith.waystones.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;
import wraith.waystones.Utils;
import wraith.waystones.block.WaystoneBlockEntity;

public final class BlockEntityRegistry {

    public static final BlockEntityType<WaystoneBlockEntity> WAYSTONE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(WaystoneBlockEntity::new, BlockRegistry.WAYSTONE).build(null);

    public static  void registerBlockEntities() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Utils.ID("waystone"), WAYSTONE_BLOCK_ENTITY);
    }

}
