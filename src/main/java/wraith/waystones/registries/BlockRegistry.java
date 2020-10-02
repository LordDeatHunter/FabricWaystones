package wraith.waystones.registries;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.waystones.Waystones;
import wraith.waystones.block.*;

public class BlockRegistry {

    public static final Block WAYSTONE = new WaystoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, 1).strength(4f, 6f));

    public static void registerBlocks(){
        Registry.register(Registry.BLOCK, new Identifier(Waystones.MOD_ID, "waystone"), WAYSTONE);
    }

}
