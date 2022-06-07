package wraith.fwaystones.util;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import wraith.fwaystones.registry.BlockRegistry;

public class CustomItemGroup {

    public static final ItemGroup WAYSTONE_GROUP = FabricItemGroupBuilder.create(Utils.ID("fwaystones")).icon(() -> new ItemStack(BlockRegistry.WAYSTONE)).build();

}
