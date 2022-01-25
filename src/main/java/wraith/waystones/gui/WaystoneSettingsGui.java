package wraith.waystones.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import wraith.waystones.Waystones;
import wraith.waystones.block.WaystoneBlockEntity;

public class WaystoneSettingsGui extends SimpleGui {
    private final WaystoneBlockEntity waystone;

    public WaystoneSettingsGui(ServerPlayerEntity player, WaystoneBlockEntity waystone) {
        super(ScreenHandlerType.HOPPER, player, false);
        this.setTitle(new TranslatableText("polyport.waystones.settings"));
        this.waystone = waystone;
    }

    @Override
    public void onTick() {
         if (waystone.isRemoved()) {
             this.close();
         }
    }

    protected void updateDisplay() {
        this.setSlot(0, new GuiElementBuilder(waystone.isGlobal() ? Items.ENDER_EYE : Items.ENDER_PEARL)
                .setName(new TranslatableText("waystones.config.global"))
                .setCallback((x, y, z) -> {
                    Waystones.WAYSTONE_STORAGE.toggleGlobal(waystone.getHash());
                    PagedGui.playClickSound(this.player);
                    this.updateDisplay();
                })
        );

        this.setSlot(1, new GuiElementBuilder(Items.SKELETON_SKULL)
                .setName(new TranslatableText("waystones.config.tooltip.revoke_ownership").formatted(Formatting.WHITE))
                .setCallback((x, y, z) -> {
                    Waystones.WAYSTONE_STORAGE.setOwner(waystone.getHash(), null);
                    PagedGui.playClickSound(this.player);
                    this.close();
                })
        );

        this.setSlot(2, new GuiElementBuilder(Items.NAME_TAG)
                .setName(new TranslatableText("polyport.waystones.rename").formatted(Formatting.WHITE))
                .setCallback((x, y, z) -> {
                    PagedGui.playClickSound(this.player);
                    openRanaming(this.player, this.waystone);
                })
        );

        this.setSlot(4, new GuiElementBuilder(Items.BARRIER)
                .setName(new TranslatableText("gui.back"))
                .setCallback((x, y, z) -> {
                    PagedGui.playClickSound(this.player);
                    UniversalWaystoneGui.open(this.player, this.waystone);
                })
        );
    }

    private static void openRanaming(ServerPlayerEntity player, WaystoneBlockEntity waystone) {
        var ui = new AnvilInputGui(player, false);
        ui.setTitle(new TranslatableText("polyport.waystones.rename"));
        ui.setDefaultInputValue(waystone.getWaystoneName());

        ui.setSlot(1,
                new GuiElementBuilder(Items.SLIME_BALL)
                        .setName(new TranslatableText("waystones.config.tooltip.set_name").formatted(Formatting.GREEN))
                        .setCallback((index, clickType, actionType) -> {
                            PagedGui.playClickSound(player);
                            Waystones.WAYSTONE_STORAGE.renameWaystone(waystone, ui.getInput());
                            ui.close();
                        })
        );

        ui.setSlot(2,
                new GuiElementBuilder(Items.BARRIER)
                        .setName(new TranslatableText("gui.back").formatted(Formatting.RED))
                        .setCallback((index, clickType, actionType) -> {
                            PagedGui.playClickSound(player);
                            WaystoneSettingsGui.open(player, waystone);
                        })
        );

        ui.open();
    }


    public static void open(ServerPlayerEntity user, WaystoneBlockEntity waystone) {
        var gui = new WaystoneSettingsGui(user, waystone);
        gui.updateDisplay();
        gui.open();
    }
}
