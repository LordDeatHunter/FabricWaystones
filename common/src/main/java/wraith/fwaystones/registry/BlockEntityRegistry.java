package wraith.fwaystones.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlockEntity;

public final class BlockEntityRegistry {
	public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITY_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
	//-------------------------------------------------------------------------------------------------------
	public static final RegistrySupplier<BlockEntityType<WaystoneBlockEntity>> WAYSTONE_BLOCK_ENTITY = BLOCKENTITY_REGISTRY.register("waystone",
			()-> BlockEntityType.Builder.of(WaystoneBlockEntity::new,
					BlockRegistry.WAYSTONE.get(),
					BlockRegistry.DESERT_WAYSTONE.get(),
					BlockRegistry.STONE_BRICK_WAYSTONE.get(),
					BlockRegistry.RED_DESERT_WAYSTONE.get(),
					BlockRegistry.RED_NETHER_BRICK_WAYSTONE.get(),
					BlockRegistry.NETHER_BRICK_WAYSTONE.get(),
					BlockRegistry.ENDSTONE_BRICK_WAYSTONE.get(),
					BlockRegistry.DEEPSLATE_BRICK_WAYSTONE.get(),
					BlockRegistry.BLACKSTONE_BRICK_WAYSTONE.get()
			).build(null));
}