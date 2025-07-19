package wraith.fwaystones.registry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.block.WaystoneBlockEntity;

public final class WaystoneBlockEntities {

    public static final BlockEntityType<WaystoneBlockEntity> WAYSTONE_BLOCK_ENTITY = BlockEntityType.Builder.create(WaystoneBlockEntity::new).build(null);

    public static void init() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, FabricWaystones.id("waystone"), WAYSTONE_BLOCK_ENTITY);
    }

}
