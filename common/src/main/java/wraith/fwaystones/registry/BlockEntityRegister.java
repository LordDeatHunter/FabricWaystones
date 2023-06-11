package wraith.fwaystones.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlockEntity;

public final class BlockEntityRegister {
	public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITY_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
	//-------------------------------------------------------------------------------------------------------
	public static final RegistrySupplier<BlockEntityType<WaystoneBlockEntity>> WAYSTONE_BLOCK_ENTITY = BLOCKENTITY_REGISTRY.register("waystone",
			()-> BlockEntityType.Builder.of(WaystoneBlockEntity::new,
					BlockRegister.WAYSTONE.get(),
					BlockRegister.DESERT_WAYSTONE.get(),
					BlockRegister.STONE_BRICK_WAYSTONE.get(),
					BlockRegister.RED_DESERT_WAYSTONE.get(),
					BlockRegister.RED_NETHER_BRICK_WAYSTONE.get(),
					BlockRegister.NETHER_BRICK_WAYSTONE.get(),
					BlockRegister.ENDSTONE_BRICK_WAYSTONE.get(),
					BlockRegister.DEEPSLATE_BRICK_WAYSTONE.get(),
					BlockRegister.BLACKSTONE_BRICK_WAYSTONE.get()
			).build(null));
}
