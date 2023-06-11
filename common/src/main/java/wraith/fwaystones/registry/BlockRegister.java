package wraith.fwaystones.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlock;

public final class BlockRegister {
	public static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registries.BLOCK);
	//-------------------------------------------------------------------------------------------------------
	public static final RegistrySupplier<Block> BLACKSTONE_BRICK_WAYSTONE = registerWaystone("blackstone_brick_waystone");
	public static final RegistrySupplier<Block> DEEPSLATE_BRICK_WAYSTONE = registerWaystone("deepslate_brick_waystone");
	public static final RegistrySupplier<Block> DESERT_WAYSTONE = registerWaystone("desert_waystone");
	public static final RegistrySupplier<Block> ENDSTONE_BRICK_WAYSTONE = registerWaystone("end_stone_brick_waystone");
	public static final RegistrySupplier<Block> NETHER_BRICK_WAYSTONE = registerWaystone("nether_brick_waystone");
	public static final RegistrySupplier<Block> RED_DESERT_WAYSTONE = registerWaystone("red_desert_waystone");
	public static final RegistrySupplier<Block> RED_NETHER_BRICK_WAYSTONE = registerWaystone("red_nether_brick_waystone");
	public static final RegistrySupplier<Block> STONE_BRICK_WAYSTONE = registerWaystone("stone_brick_waystone");
	public static final RegistrySupplier<Block> WAYSTONE = registerWaystone("waystone");
	//-------------------------------------------------------------------------------------------------------
	// TODO: INJECT MINING LEVEL TAG
	private static ResourceLocation miningLevelTag;
	private static RegistrySupplier<Block> registerWaystone(String name) {
		RegistrySupplier<Block> block = BLOCK_REGISTRY.register(name, ()->new WaystoneBlock(
				BlockBehaviour.Properties
						.of(Material.STONE)
						.requiresCorrectToolForDrops()
						.strength(Waystones.CONFIG.waystone_block_hardness, 3600000)
		));
		var miningLevel = Waystones.CONFIG.waystone_block_required_mining_level;
		miningLevelTag = new ResourceLocation(switch (miningLevel) {
			case 1 -> "minecraft:needs_stone_tool";
			case 2 -> "minecraft:needs_iron_tool";
			case 3 -> "minecraft:needs_diamond_tool";
			default -> "fabric:needs_tool_level_" + miningLevel;
		});
		return block;
	}

}
