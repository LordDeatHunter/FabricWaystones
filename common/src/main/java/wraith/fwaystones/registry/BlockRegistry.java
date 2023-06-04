package wraith.fwaystones.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.block.WaystoneBlock;


public final class BlockRegistry {
    public static final DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registry.BLOCK_REGISTRY);
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
    private static RegistrySupplier<Block> registerWaystone(String name) {
        RegistrySupplier<Block> toReturn = BLOCK_REGISTRY.register(name, ()->new WaystoneBlock(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(Waystones.CONFIG.waystone_block_hardness, 3600000)));
        return toReturn;
    }
}
