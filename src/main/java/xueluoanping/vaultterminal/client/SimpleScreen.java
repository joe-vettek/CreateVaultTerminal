package xueluoanping.vaultterminal.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SimpleScreen extends AbstractContainerScreen<SimpleMenu> {

    public static ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");

    private int startIndex = 0;

    public SimpleScreen(SimpleMenu container, Inventory playerInv, Component name) {
        super(container, playerInv, name);
        this.imageWidth = 176;
        this.imageHeight = 199;
        // this.background = bg;
        // this.inventory = playerInv;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pGuiGraphics);
        renderLabels(pGuiGraphics,pMouseX,pMouseY);
        int lp = this.leftPos;
        int tp = this.topPos;
        pGuiGraphics.blit(BG_LOCATION, lp, tp, 0, 0, this.imageWidth, this.imageHeight - 33);

        int l = this.leftPos + 52;
        int i1 = this.topPos + 14;
        int line_count = 4;
        for (int i = 0; i + startIndex < this.menu.itemStacks.size() && i < 12; i++) {
            int ih = this.imageHeight;
            int pX = (i % line_count) * 16 + l;
            int pY = i / line_count * 18 + i1 + 1;
            pGuiGraphics.blit(BG_LOCATION, pX, pY, 0, ih - 33, 16, 18);
            ItemStack stack = this.menu.itemStacks.get(i + startIndex);
            pGuiGraphics.renderItem(stack, pX, pY + 1);
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0,0,200);
            if (stack.getCount() > 1) {
                String count = Integer.toString(stack.getCount());
                pGuiGraphics.drawString(font, count, pX + 16 - font.width(count) + 1, pY + 18 - font.lineHeight + 2, Color.WHITE.getRGB());
            }
            pGuiGraphics.pose().popPose();
        }


        int k = (int) (41.0F * (Mth.clamp((1f * startIndex) / (Math.max(this.menu.itemStacks.size() / 4 - 2, 0) * 4), 0f, 1f)));
        pGuiGraphics.blit(BG_LOCATION, lp + 119, tp + 15 + k, 176, 0, 12, 15);

        renderTooltip(pGuiGraphics,pMouseX,pMouseY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        // this.menu.setCarried(Items.APPLE.getDefaultInstance());
        if (pDelta > 0) {
            startIndex -= 4;
        } else {
            startIndex += 4;
        }
        startIndex = Mth.clamp(startIndex, 0, Math.max(this.menu.itemStacks.size() / 4 - 2, 0) * 4);
        // SafeReader.logger(pDelta, startIndex);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int ls = this.leftPos + 52;
        int ts = this.topPos + 14;
        int u = (int) ((pMouseX - ls) / 16);
        int v = (int) ((pMouseY - ts) / 18);
        if (u >= 0 && u <= 4
                && v >= 0 && v <= 3) {
            int bid = u + v * 4 + startIndex;
            // SafeReader.logger(bid,u,v);

            if (bid < this.menu.itemStacks.size()) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, bid);
                this.menu.itemStacks.remove(bid);
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);

    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
        int ls = this.leftPos + 52;
        int ts = this.topPos + 14;
        int u = (int) ((pX - ls) / 16);
        int v = (int) ((pY - ts) / 18);
        if (u >= 0 && u <= 4
                && v >= 0 && v <= 3) {
            int bid = u + v * 4 + startIndex;
            if (bid < this.menu.itemStacks.size()) {
                pGuiGraphics.renderTooltip(font, this.menu.itemStacks.get(bid), pX, pY);
            }
        }
    }
}
