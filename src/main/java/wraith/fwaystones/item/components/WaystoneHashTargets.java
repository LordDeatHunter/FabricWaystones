package wraith.fwaystones.item.components;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.*;
import java.util.function.Consumer;

public record WaystoneHashTargets(List<UUID> ids) implements ExtendedTooltipAppender {

    public static final WaystoneHashTargets EMPTY = new WaystoneHashTargets(List.of());

    public static final StructEndec<WaystoneHashTargets> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.listOf().fieldOf("hashes", WaystoneHashTargets::ids),
            WaystoneHashTargets::new
    );

    public static WaystoneHashTargets get(ItemStack stack, @Nullable World world) {
        WaystoneHashTargets targets = EMPTY;
        if (!stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
            var component = stack.get(DataComponentTypes.CUSTOM_DATA);

            if (component != null) {
                var tag = component.getNbt();

                if (tag != null && tag.contains(FabricWaystones.MOD_ID)) {
                    var list = tag.getList(FabricWaystones.MOD_ID, NbtElement.STRING_TYPE);
                    var hashes = new ArrayList<UUID>();

                    if (world != null) {
                        var data = WaystoneDataStorage.getStorage(world);

                        for (int i = 0; i < list.size(); ++i) {
                            String hashString = list.getString(i);

                            var position = WaystonePosition.unsafePositonFromHash(hashString, world.getScoreboard());

                            if (!position.isUnsafe()) {
                                var uuid = data.getUUID(position);

                                hashes.add(uuid);
                            }
                        }
                    }

                    tag.remove(FabricWaystones.MOD_ID);

                    NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, tag);

                    targets = new WaystoneHashTargets(hashes);
                }
            }

            if (world != null && !world.isClient()) {
                stack.set(WaystoneDataComponents.HASH_TARGETS, targets);
            }
        } else {
            targets = stack.getOrDefault(WaystoneDataComponents.HASH_TARGETS, EMPTY);
        }

        return targets;
    }

    public WaystoneHashTargets addHash(UUID uuid) {
        var list = new ArrayList<>(this.ids());

        list.add(uuid);

        return new WaystoneHashTargets(Collections.unmodifiableList(list));
    }

    public WaystoneHashTargets removeHash(UUID uuid) {
        var list = new ArrayList<>(this.ids());

        list.remove(uuid);

        return new WaystoneHashTargets(Collections.unmodifiableList(list));
    }

    public WaystoneHashTargets removeHashes(Collection<UUID> ids) {
        var list = new ArrayList<>(this.ids());

        list.removeAll(ids);

        return new WaystoneHashTargets(Collections.unmodifiableList(list));
    }

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        var hashes = WaystoneHashTargets.get(stack, null).ids();

        int size = hashes.size();
        Set<WaystonePosition> waystones = null;
        var storage = WaystoneDataStorage.getStorageUnsafe();
        if (storage != null) waystones = storage.getAllPositions();

        if (waystones != null) {
            tooltip.accept(TextUtils.translationWithArg("scroll.tooltip", String.valueOf(size)));
        }
    }
}
