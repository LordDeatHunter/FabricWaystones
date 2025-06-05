package wraith.fwaystones.networking.packets.s2c;

import io.wispforest.endec.StructEndec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import wraith.fwaystones.registry.WaystoneItems;

public record VoidRevive() {
    public static final StructEndec<VoidRevive> ENDEC = StructEndec.unit(VoidRevive::new);

    @Environment(EnvType.CLIENT)
    public static void handle(VoidRevive packet, PlayerEntity player) {
        var client = MinecraftClient.getInstance();

        client.particleManager.addEmitter(player, ParticleTypes.TOTEM_OF_UNDYING, 30);

        player.getWorld().playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE, player.getSoundCategory(), 1.0F, 1.0F, false);

        for (int i = 0; i < player.getInventory().size(); ++i) {
            ItemStack playerStack = player.getInventory().getStack(i);
            if (playerStack.getItem() == WaystoneItems.get("void_totem")) {
                client.gameRenderer.showFloatingItem(playerStack);
                break;
            }
        }
    }
}
