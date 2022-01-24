package wraith.waystones.util;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import wraith.waystones.registry.BlockRegistry;

public class CustomItemGroup {
    public static final ItemGroup WAYSTONE_GROUP = PolymerItemGroup.create(Utils.ID("waystones"), new TranslatableText("itemGroup.waystones.waystones"), () -> new ItemStack(BlockRegistry.WAYSTONE));

}
