package wraith.waystones.item;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.block.WaystoneBlock;

import java.util.List;

public class WaystoneItem extends BlockItem implements PolymerItem {

    public WaystoneItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null) {
            return;
        }
        String name = tag.getString("waystone_name");
        boolean global = tag.getBoolean("waystone_is_global");
        tooltip.add(new TranslatableText(
                "waystones.waystone_tooltip.name",
                new LiteralText(name).styled(style ->
                        style.withColor(TextColor.parse(new TranslatableText("waystones.waystone_tooltip.name.arg_color").getString()))
                )
        ));
        tooltip.add(new TranslatableText("waystones.waystone_tooltip.global").append(" ")
                .append(new TranslatableText("waystones.waystone_tooltip.global_" + (global ? "on" : "off"))));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return ((WaystoneBlock) this.getBlock()).getStyle().lower().getBlock().asItem();
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        var stack = PolymerItem.super.getPolymerItemStack(itemStack, player);
        stack.addEnchantment(Enchantments.LURE, 2);
        return stack;
    }
}
