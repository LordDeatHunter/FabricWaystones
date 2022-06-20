package wraith.fwaystones.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.access.PlayerEntityMixinAccess;
import wraith.fwaystones.block.WaystoneBlock;

import java.util.HashSet;
import java.util.List;

public class WaystoneScrollItem extends Item {

    public WaystoneScrollItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return TypedActionResult.fail(stack);
        }
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return TypedActionResult.fail(stack);
        }
        NbtList list = tag.getList(FabricWaystones.MOD_ID, NbtElement.STRING_TYPE);
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (int i = 0; i < list.size(); ++i) {
            String hash = list.getString(i);
            if (FabricWaystones.WAYSTONE_STORAGE.containsHash(hash) && !((PlayerEntityMixinAccess) user).hasDiscoveredWaystone(hash)) {
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
                        style.withColor(TextColor.parse(Text.translatable("fwaystones.learned.multiple.arg_color").getString()))
                    )
                );
            } else {
                text = Text.translatable("fwaystones.learned.single");
            }
            ((PlayerEntityMixinAccess) user).discoverWaystones(toLearn);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        } else {
            text = Text.translatable("fwaystones.learned.none");
            stack.setNbt(null);
        }
        user.sendMessage(text, false);

        if (stack.isEmpty()) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        stack = user.getStackInHand(hand);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().getBlockState(context.getBlockPos()).getBlock() instanceof WaystoneBlock && context.getPlayer() != null) {
            var discovered = ((PlayerEntityMixinAccess) context.getPlayer()).getDiscoveredWaystones();

            ItemStack stack = context.getStack();

            if (discovered.isEmpty()) {
                return ActionResult.FAIL;
            }
            NbtCompound tag = new NbtCompound();
            NbtList list = new NbtList();
            for (String hash : discovered) {
                list.add(NbtString.of(hash));
            }
            tag.put(FabricWaystones.MOD_ID, list);
            stack.setNbt(tag);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return;
        }
        int size = tag.getList(FabricWaystones.MOD_ID, NbtElement.STRING_TYPE).size();
        HashSet<String> waystones = null;
        if (FabricWaystones.WAYSTONE_STORAGE != null) {
            waystones = FabricWaystones.WAYSTONE_STORAGE.getAllHashes();
        }
        if (waystones != null) {
            tooltip.add(Text.translatable(
                "fwaystones.scroll.tooltip",
                Text.literal(String.valueOf(size)).styled(style ->
                    style.withColor(TextColor.parse(Text.translatable("fwaystones.scroll.tooltip.arg_color").getString()))
                )
            ));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return "item.fwaystones.empty_scroll";
        }
        return "item.fwaystones.waystone_scroll";
    }
}
