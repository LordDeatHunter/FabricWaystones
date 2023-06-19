package wraith.fwaystones.registry;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import wraith.fwaystones.Waystones;
import wraith.fwaystones.screen.AbyssScreenHandler;
import wraith.fwaystones.screen.PocketWormholeScreenHandler;
import wraith.fwaystones.screen.UniversalWaystoneScreenHandler;
import wraith.fwaystones.screen.WaystoneBlockScreenHandler;

public class MenuRegister {
	public static final DeferredRegister<MenuType<?>> MENU_REGISTRY = DeferredRegister.create(Waystones.MOD_ID, Registries.MENU);
	public static final RegistrySupplier<MenuType<? extends UniversalWaystoneScreenHandler>> ABYSS_MENU = MENU_REGISTRY.register("abyss", () -> new MenuType<>(AbyssScreenHandler::new, FeatureFlags.VANILLA_SET));
	public static final RegistrySupplier<MenuType<? extends UniversalWaystoneScreenHandler>> WAYSTONE_MENU = MENU_REGISTRY.register("waystone", () -> MenuRegistry.ofExtended(WaystoneBlockScreenHandler::new));
	public static final RegistrySupplier<MenuType<? extends UniversalWaystoneScreenHandler>> POCKET_WORMHOLE_MENU = MENU_REGISTRY.register("pocket_wormhole", () -> new MenuType<>(PocketWormholeScreenHandler::new, FeatureFlags.VANILLA_SET));
}
