package wraith.fwaystones.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
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
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (FabricWaystones.WAYSTONE_STORAGE == null) {
            return ActionResult.FAIL;
        }
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) {
            return null;
        }
        NbtCompound tag = component.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return ActionResult.FAIL;
        }
        NbtList list = tag.getList(FabricWaystones.MOD_ID, NbtElement.STRING_TYPE);
        int learned = 0;
        HashSet<String> toLearn = new HashSet<>();
        for (int i = 0; i < list.size(); ++i) {
            String hash = list.getString(i);
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
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
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
            NbtCompound tag = new NbtCompound();
            NbtList list = new NbtList();
            for (String hash : discovered) {
                list.add(NbtString.of(hash));
            }
            tag.put(FabricWaystones.MOD_ID, list);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));

            context.getPlayer().setStackInHand(context.getHand(), stack);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) {
            return;
        }
        NbtCompound tag = component.getNbt();
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
                    style.withColor(TextColor.parse(Text.translatable("fwaystones.scroll.tooltip.arg_color").getString()).getOrThrow())
                )
            ));
        }
    }

    public String getTranslationKey(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) {
            return "item.fwaystones.empty_scroll";
        }
        NbtCompound tag = component.getNbt();
        if (tag == null || !tag.contains(FabricWaystones.MOD_ID)) {
            return "item.fwaystones.empty_scroll";
        }
        return "item.fwaystones.waystone_scroll";
    }
}
