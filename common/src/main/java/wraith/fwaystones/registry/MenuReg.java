package wraith.fwaystones.registry;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.screen.*;

public class MenuReg {

	public static final DeferredRegister<MenuType<?>> MENU_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registry.MENU_REGISTRY);
	public static final RegistrySupplier<MenuType<? extends Universalmenu>> ABYSS_MENU = MENU_REGISTRY.register("abyss", () -> new MenuType<>(AbyssMenu::new));
	public static final RegistrySupplier<MenuType<? extends Universalmenu>> WAYSTONE_MENU = MENU_REGISTRY.register("waystone", () -> MenuRegistry.ofExtended(WaystoneMenu::new));
	public static final RegistrySupplier<MenuType<? extends Universalmenu>> POCKET_WORMHOLE_MENU = MENU_REGISTRY.register("pocket_wormhole", () -> new MenuType<>(PocketWormholeMenu::new));
}
