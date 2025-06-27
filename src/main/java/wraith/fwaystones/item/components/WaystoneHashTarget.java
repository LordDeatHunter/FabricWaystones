package wraith.fwaystones.item.components;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fwaystones.FabricWaystones;
import wraith.fwaystones.api.core.NetworkedWaystoneData;
import wraith.fwaystones.api.core.WaystoneData;
import wraith.fwaystones.item.WaystoneComponentEventHooks;
import wraith.fwaystones.registry.WaystoneDataComponents;
import wraith.fwaystones.api.WaystoneDataStorage;
import wraith.fwaystones.api.core.WaystonePosition;

import java.util.UUID;
import java.util.function.Consumer;

public record WaystoneHashTarget(UUID uuid, @Nullable Boolean allowTeleportOnUse) implements ExtendedTooltipAppender {

    public static final StructEndec<WaystoneHashTarget> ENDEC = StructEndecBuilder.of(
            BuiltInEndecs.UUID.fieldOf("uuid", WaystoneHashTarget::uuid),
            Endec.BOOLEAN.optionalFieldOf("allow_teleport_on_use", WaystoneHashTarget::allowTeleportOnUse, () -> null),
            WaystoneHashTarget::new
    );

    @Nullable
    public static WaystoneHashTarget get(ItemStack stack, @Nullable World world) {
        if (stack.contains(WaystoneDataComponents.HASH_TARGETS)) {
            var component = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (component == null) return null;

            var tag = component.getNbt();

            if (tag != null && tag.contains(FabricWaystones.MOD_ID)) {
                var hashString = tag.getString(FabricWaystones.MOD_ID);

                if (world != null) {
                    var position = WaystonePosition.unsafePositonFromHash(hashString, world.getScoreboard());

                    if (!position.isUnsafe()) {
                        var uuid = WaystoneDataStorage.getStorage(world).getUUID(position);

                        var target = new WaystoneHashTarget(uuid, stack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEM));

                        if (!world.isClient()) {
                            stack.set(WaystoneDataComponents.HASH_TARGET, target);
                        }

                        return target;
                    }

                }
            }
        } else {
            return stack.get(WaystoneDataComponents.HASH_TARGET);
        }

        return null;
    }

    @Override
    public void appendTooltip(@Nullable ItemStack stack, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {
        Text name = null;

        var storage = WaystoneDataStorage.getStorageUnsafe();

        var uuid = uuid();
        var translationName = WaystoneComponentEventHooks.getLocalVoidName(stack);

        if (uuid == WaystoneData.EMPTY_UUID) {
            tooltip.accept(Text.translatable(TextUtils.translationKey(translationName + ".empty_tooltip")));
            return;
        }

        if (storage != null) {
            var data = storage.getData(uuid);

            if (data != null) name = data.parsedName();
        }

        if (name != null) {
            tooltip.accept(TextUtils.translationWithArg(translationName + ".tooltip", name));
        }

        var cooldowns = FabricWaystones.CONFIG.teleportCooldowns;

        if (stack != null) {
            var canTeleport = stack.isIn(FabricWaystones.DIRECTED_TELEPORT_ITEM);

            var allowTeleportOnUse = this.allowTeleportOnUse;

            if (allowTeleportOnUse == null) allowTeleportOnUse = canTeleport;

            var cooldownAmount = allowTeleportOnUse ? cooldowns.usedLocalVoid() : cooldowns.usedVoidTotem();

            if (cooldownAmount > 0) {
                tooltip.accept(TextUtils.translationWithArg("cool_down.tooltip", String.valueOf(cooldownAmount / 20)));
            }
        }
    }
}
