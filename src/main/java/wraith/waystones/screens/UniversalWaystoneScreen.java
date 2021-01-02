package wraith.waystones.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import wraith.waystones.Config;
import wraith.waystones.Waystones;

public class UniversalWaystoneScreen extends HandledScreen<ScreenHandler> {

    private final Identifier texture;
    protected float scrollAmount;
    protected boolean mouseClicked;
    protected int scrollOffset;

    public UniversalWaystoneScreen(ScreenHandler handler, PlayerInventory inventory, Identifier texture, Text title) {
        super(handler, inventory, title);
        this.texture = texture;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(texture);
        this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int k = (int)(41.0F * this.scrollAmount);
        this.drawTexture(matrices, x + 140, y + 35 + k, 177 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
        int l = this.x + 36;
        int m = this.y + 34;
        int n = this.scrollOffset + 3;
        this.renderWaystoneBackground(matrices, mouseX, mouseY, l, m, n);
        this.renderForgetButtons(matrices, mouseX, mouseY, x + 25, y + 40);
        this.renderCostItem(matrices, x + 34, y + 90);
        this.renderWaystoneNames(matrices, l, m, n);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    protected void renderCostItem(MatrixStack matrices, int x, int y) {
        String text;
        switch(Config.getInstance().teleportType())
        {
            case "xp":
                this.drawTexture(matrices, x+4, y +4, 177, 15, 9, 9);
                text = "XP";
                break;
            case "level":
                this.itemRenderer.renderGuiItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), x, y);
                text = "Levels";
                break;
            default:
                this.itemRenderer.renderGuiItemIcon(new ItemStack(Registry.ITEM.get(Config.getInstance().teleportCostItem())), x, y);
                text = "Total";
                break;
        }

        this.textRenderer.draw(matrices, text + ": " + Config.getInstance().teleportCost(), x + 16f, y + 5f, 0x161616);
    }
    protected void renderForgetButtons(MatrixStack matrixStack, int mouseX, int mouseY, int x, int y) {
        int n = Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player);
        for (int i = 0; i < 3; ++i) {
            int r = y + i * 18;
            int v = 0;
            if (i >= n) {
                v = 7;
            }
            else if (mouseX >= x && mouseY >= r && mouseX < x + 7 && mouseY < r + 7) {
                if (mouseClicked) {
                    v += 7;
                } else {
                    v += 14;
                }
            }
            this.drawTexture(matrixStack, x, r, 201, v, 7, 7);
        }
    }

    protected void renderWaystoneBackground(MatrixStack matrixStack, int mouseX, int mouseY, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;
            int s = this.backgroundHeight;
            if (mouseX >= k && mouseY >= r && mouseX < k + 101 && mouseY < r + 18) {
                if (mouseClicked) {
                    s += 18;
                } else {
                    s += 36;
                }
            }
            this.drawTexture(matrixStack, k, r - 1, 0, s, 101, 18);
        }
    }

    protected void renderWaystoneNames(MatrixStack matrices, int k, int l, int m) {
        for(int n = this.scrollOffset; n < m && n < Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player); ++n) {
            int o = n - this.scrollOffset;
            int r = l + o * 18 + 2;

            this.textRenderer.draw(matrices, Waystones.WAYSTONE_DATABASE.getDiscoveredWaystoneNames(playerInventory.player).get(n), k + 5f, r - 1 + 5f, 0x161616);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        if (this.hasWaystones()) {
            int i1 = this.x + 25;
            int j1 = this.y + 40;
            int i2 = this.x + 36;
            int j2 = this.y + 35;
            int k = this.scrollOffset + 3;

            int n = Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player);
            for(int l = this.scrollOffset; l < k; ++l) {
                int m = l - this.scrollOffset;
                double x1 = mouseX - (double)(i1);
                double y1 = mouseY - (double)(j1 + m * 18);

                double x2 = mouseX - (double)(i2);
                double y2 = mouseY - (double)(j2 + m * 18);
                if (m < n && x1 >= 0.0D && y1 >= 0.0D && x1 < 7 && y1 < 7 && (this.handler).onButtonClick(this.client.player, l * 2 + 1)) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_ANVIL_BREAK, 1.0F));
                    this.scrollOffset = Math.max(0, this.scrollOffset - 1);
                    this.client.interactionManager.clickButton((this.handler).syncId, l * 2 + 1);
                    return true;
                }
                if (x2 >= 0.0D && y2 >= 0.0D && x2 < 101.0D && y2 < 18.0D && (this.handler).onButtonClick(this.client.player, l * 2)) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F));
                    this.client.interactionManager.clickButton((this.handler).syncId, l * 2);
                    return true;
                }
            }

            int i3 = this.x + 140;
            int j3 = this.y + 35;
            if (mouseX >= (double)i3 && mouseX < (double)(i3 + 12) && mouseY >= (double)j3 && mouseY < (double)(j3 + 54)) {
                this.mouseClicked = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            this.scrollAmount = (float)((double)this.scrollAmount - amount / (double)i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)i) + 0.5D);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + 54;
            this.scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)((double)(this.scrollAmount * (float)this.getMaxScroll()) + 0.5D);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    protected boolean hasWaystones() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) > 0;
    }

    protected boolean shouldScroll() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) > 3;
    }

    protected int getMaxScroll() {
        return Waystones.WAYSTONE_DATABASE.getPlayerDiscoveredCount(playerInventory.player) - 3;
    }


}
