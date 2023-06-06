package wraith.fwaystones.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlockEntity;

public final class BlockEntityReg {
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITY_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    //-------------------------------------------------------------------------------------------------------
    public static final RegistrySupplier<BlockEntityType<WaystoneBlockEntity>> WAYSTONE_BLOCK_ENTITY =
            BLOCKENTITY_REGISTRY.register("waystone", () ->
                    BlockEntityType.Builder.of(WaystoneBlockEntity::new,
                            BlockReg.WAYSTONE.get(),
                            BlockReg.DESERT_WAYSTONE.get(),
                            BlockReg.STONE_BRICK_WAYSTONE.get(),
                            BlockReg.RED_DESERT_WAYSTONE.get(),
                            BlockReg.RED_NETHER_BRICK_WAYSTONE.get(),
                            BlockReg.NETHER_BRICK_WAYSTONE.get(),
                            BlockReg.ENDSTONE_BRICK_WAYSTONE.get(),
                            BlockReg.DEEPSLATE_BRICK_WAYSTONE.get(),
                            BlockReg.BLACKSTONE_BRICK_WAYSTONE.get()
                    ).build(null));
}
