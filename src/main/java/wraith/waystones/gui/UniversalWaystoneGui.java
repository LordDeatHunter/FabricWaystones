package wraith.waystones.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import wraith.waystones.Waystones;
import wraith.waystones.access.PlayerAccess;
import wraith.waystones.access.PlayerEntityMixinAccess;
import wraith.waystones.block.WaystoneBlock;
import wraith.waystones.block.WaystoneBlockEntity;
import wraith.waystones.item.AbyssWatcherItem;
import wraith.waystones.util.Config;
import wraith.waystones.util.TeleportSources;
import wraith.waystones.util.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UniversalWaystoneGui extends PagedGui {
    private final Predicate<UniversalWaystoneGui> keepOpen;
    private final TeleportSources source;
    protected boolean teleported = false;
    private ArrayList<String> sortedWaystones;

    protected UniversalWaystoneGui(ServerPlayerEntity player, Text title, TeleportSources source, Predicate<UniversalWaystoneGui> keepOpen, @Nullable Consumer<UniversalWaystoneGui> closeCallback) {
        super(player, closeCallback);
        this.keepOpen = keepOpen;
        this.source = source;
        this.setTitle(title);
    }

    public static void open(ServerPlayerEntity user, Text title, TeleportSources source) {
        var ui = new UniversalWaystoneGui(user, title, source, source == TeleportSources.ABYSS_WATCHER ? t -> {
            for (var hand : Hand.values()) {
                if (user.getStackInHand(hand).getItem() instanceof AbyssWatcherItem) {
                    return true;
                }
            }
            return false;
        } : t -> true, (gui) -> {});

        ui.updateDisplay();
        ui.open();
    }

    public static void open(ServerPlayerEntity user, WaystoneBlockEntity waystone) {
        var ui = new UniversalWaystoneGui(user, new LiteralText(waystone.getWaystoneName()), TeleportSources.WAYSTONE,
                t -> !waystone.isRemoved() && ((PlayerEntityMixinAccess) user).hasDiscoveredWaystone(waystone),
                (gui) -> {}
        ) {
            @Override
            protected DisplayElement getNavElement(int id) {
                return switch (id) {
                    case 1 -> DisplayElement.previousPage(this);
                    case 3 -> DisplayElement.nextPage(this);
                    case 5 -> getCost();
                    case 7 -> DisplayElement.of(
                            new GuiElementBuilder(Items.REDSTONE)
                            .setName(new TranslatableText("waystones.config.tooltip.config"))
                    );
                    default -> DisplayElement.filler();
                };
            }
        };

        ui.updateDisplay();
        ui.open();
    }


    public TeleportSources getSource() {
        return this.source;
    }

    @Override
    public void onTick() {
        if (!this.keepOpen.test(this)) {
            this.close();
        }
    }

    @Override
    protected void updateDisplay() {
        this.updateWaystones();
        super.updateDisplay();
    }

    private void updateWaystones() {
        this.sortedWaystones = new ArrayList<>();
        if (((PlayerEntityMixinAccess) player).shouldViewDiscoveredWaystones()) {
            this.sortedWaystones.addAll(((PlayerAccess) player).getHashesSorted());
        }
        if (((PlayerEntityMixinAccess) player).shouldViewGlobalWaystones()) {
            for (String waystone : Waystones.WAYSTONE_STORAGE.getGlobals()) {
                if (!this.sortedWaystones.contains(waystone)) {
                    this.sortedWaystones.add(waystone);
                }
            }
        }
        this.sortedWaystones.sort(Comparator.comparing(a -> Waystones.WAYSTONE_STORAGE.getWaystone(a).getWaystoneName()));
    }

    @Override
    protected int getPageAmount() {
        return this.sortedWaystones.size() / PAGE_SIZE;
    }

    @Override
    protected DisplayElement getElement(int id) {
        if (this.sortedWaystones.size() > id) {
            var hash = this.sortedWaystones.get(id);
            var tmpWaystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);


            return DisplayElement.of(
                    new GuiElementBuilder(tmpWaystone.isGlobal() ? Items.ENDER_EYE : Items.ENDER_PEARL)
                            .setName(new LiteralText(tmpWaystone.getWaystoneName()))
                            .setCallback((x, y, z) -> this.handleSelection(x, y, z, hash))
            );
        }

        return DisplayElement.empty();
    }

    private void handleSelection(int i, ClickType type, SlotActionType actionType, String hash) {
        var waystone = Waystones.WAYSTONE_STORAGE.getWaystone(hash);
        if (waystone == null) {
            return;
        }
        if (type.shift && type.isRight) {
            if (waystone.isGlobal()) {
                return;
            }
            if (player.getUuid().equals(waystone.getOwner())) {
                waystone.setOwner(null);
            }
            ((PlayerEntityMixinAccess) player).forgetWaystone(hash);
            this.updateDisplay();
        } else if (type.isLeft) {
            if (waystone.getWorld() != null && !(waystone.getWorld().getBlockState(waystone.getPos()).getBlock() instanceof WaystoneBlock)) {
                Waystones.WAYSTONE_STORAGE.removeWaystone(hash);
                waystone.getWorld().removeBlockEntity(waystone.getPos());
            } else {
                if (Utils.canTeleport(player,  hash, false)) {
                    this.teleported = waystone.teleportPlayer(player, true);
                    this.close();
                }
            }
        }

    }

    @Override
    protected DisplayElement getNavElement(int id) {
        return switch (id) {
            case 1 -> DisplayElement.previousPage(this);
            case 3 -> DisplayElement.nextPage(this);
            case 6 -> getCost();
            default -> DisplayElement.filler();
        };
    }

    protected DisplayElement getCost() {
            String cost = Config.getInstance().teleportType();
            int amount = Config.getInstance().teleportCost();

            Item item;
            String type;

            switch (cost) {
                case "hp":
                case "health":
                    item = Items.RED_DYE;
                    type = "health";
                    break;
                case "hunger":
                case "saturation":
                    item = Items.PORKCHOP;
                    type = "hunger";
                    break;
                case "xp":
                case "experience":
                    item = Items.EXPERIENCE_BOTTLE;
                    type = "xp";
                    break;
                case "level":
                    item = Items.EXPERIENCE_BOTTLE;
                    type = "level";
                    break;
                case "item":
                    return DisplayElement.of(new GuiElement(new ItemStack(Registry.ITEM.get(Config.getInstance().teleportCostItem()), amount), GuiElement.EMPTY_CALLBACK));
                default:
                    return DisplayElement.filler();
            }

            return DisplayElement.of(new GuiElementBuilder(item)
                    .setName(new TranslatableText("waystones.cost." + type))
                    .setCount(amount));
    }
}
