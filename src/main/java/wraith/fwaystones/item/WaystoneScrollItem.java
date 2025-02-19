package wraith.fwaystones.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;
import wraith.fwaystones.registry.DataComponentRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class WaystoneScrollItem extends Item {

    public WaystoneScrollItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return ActionResult.FAIL;
        }
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        if (waystones == null || waystones.isEmpty()) {
            return ActionResult.FAIL;
        }
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (String hash : waystones) {
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) user).fabricWaystones$hasDiscoveredWaystone(hash)) {
                var waystone = FabricWaystones.WAYSTONE_STORAGE.getWaystoneEntity(hash);
                if (waystone != null && waystone.getOwner() == null) {
                    waystone.setOwner(user);
                }
                toLearn.add(hash);
                ++learned;
            }
        }
        Text text;
        if (learned > 0) {
            if (learned > 1) {
                text = Text.translatable(
                    "fwaystones.learned.multiple",
                    Text.literal(String.valueOf(learned)).styled(style ->
                        style.withColor(TextColor.parse(Text.translatable("fwaystones.learned.multiple.arg_color").getString()).getOrThrow())
                    )
                );
            } else {
                text = Text.translatable("fwaystones.learned.single");
            }
            ((PlayerEntityMixinAccess) user).fabricWaystones$discoverWaystones(toLearn);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        } else {
            text = Text.translatable("fwaystones.learned.none");
            stack.set(DataComponentRegistry.WAYSTONES, Collections.emptyList());
        }
        user.sendMessage(text, false);

        if (stack.isEmpty()) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        stack = user.getStackInHand(hand);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof WaystoneBlock && context.getPlayer() != null) {
            var discovered = ((PlayerEntityMixinAccess) context.getPlayer()).fabricWaystones$getDiscoveredWaystones();

            ItemStack stack = context.getStack();

            if (discovered.isEmpty()) {
                return ActionResult.FAIL;
            }
            List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
            if (waystones == null) {
                waystones = discovered.stream().toList();
            } else {
                waystones = new ArrayList<>(waystones);
                waystones.addAll(discovered);
            }
            stack.set(DataComponentRegistry.WAYSTONES, waystones);

            return ActionResult.SUCCESS.withNewHandStack(stack);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        if (waystones == null || waystones.isEmpty()) {
            waystones = FabricWaystones.WAYSTONE_STORAGE.getAllHashes().stream().toList();
        }
        tooltip.add(Text.translatable(
            "fwaystones.scroll.tooltip",
            Text.literal(String.valueOf(waystones.size())).styled(style ->
                style.withColor(TextColor.parse(Text.translatable("fwaystones.scroll.tooltip.arg_color").getString()).getOrThrow())
            )
        ));
    }

    @Override
    public Text getName(ItemStack stack) {
        List<String> waystones = stack.get(DataComponentRegistry.WAYSTONES);
        return waystones == null || waystones.isEmpty() ? Text.translatable("item.fwaystones.empty_scroll") : Text.translatable("item.fwaystones.waystone_scroll");
    }
}
