package wraith.fwaystones.api.core;

import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ExtendedStackReference extends StackReference {

    static ExtendedStackReference of(final Supplier<ItemStack> getter, final Consumer<ItemStack> setter, Consumer<ItemStack> onBreak) {
        return new ExtendedStackReference() {
            public ItemStack get() {
                return getter.get();
            }

            public boolean set(ItemStack stack) {
                setter.accept(stack);
                return true;
            }

            @Override
            public void breakStack(ItemStack stack) {
                onBreak.accept(stack);
            }
        };
    }

    void breakStack(ItemStack stack);
}
