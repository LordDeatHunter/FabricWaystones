package wraith.waystones.mixin;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Accessor("textureManager")
    public TextureManager getTextureManager();

    @Accessor("colorMap")
    public ItemColors getColorMap();
}
