package xueluoanping.vaultterminal.client;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import xueluoanping.vaultterminal.SafeReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SimpleScreen extends AbstractContainerScreen<SimpleMenu> {

    public static ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");

    private int startIndex = 0;
    private EditBox nameEdit;

    private final List<Pair<int[], ItemStack>> pairList = new ArrayList<>();

    private boolean isDragInScroll = false;

    public SimpleScreen(SimpleMenu menu, Inventory playerInv, Component name) {
        super(menu, playerInv, name);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }


    @Override
    protected void containerTick() {
        this.nameEdit.tick();
    }

    protected void init() {
        super.init();
        this.nameEdit = new EditBox(this.font, this.leftPos + 141, this.topPos + 4, 30, font.lineHeight + 2, Component.translatable("addServer.enterName"));
        this.nameEdit.setValue("");
        this.nameEdit.setResponder((string) -> {
            this.updateAddButtonStatus();
        });
        this.addWidget(this.nameEdit);
        // init Status
        updateAddButtonStatus();
    }

    private void updateAddButtonStatus() {
        String name = nameEdit.getValue();
        this.pairList.clear();
        int i = 0;
        boolean isEmpty = name.isEmpty();
        String nameUse = name.toLowerCase(Locale.ROOT);
        Object2IntArrayMap<String> byIds = new Object2IntArrayMap<>();
        byIds.defaultReturnValue(-1);
        for (ItemStack itemStack : this.menu.itemStacks) {
            boolean should = isEmpty;
            if (!should) {
                StringBuilder stringBuilder = new StringBuilder();
                if (Minecraft.getInstance().player != null) {
                    for (Component component : itemStack.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.NORMAL)) {
                        stringBuilder.append(component.getString());
                    }
                } else {
                    stringBuilder.append(itemStack.getHoverName().getString());
                }
                should = stringBuilder.toString().toLowerCase(Locale.ROOT).contains(nameUse);
            }
            if (should) {
                itemStack = itemStack.copyWithCount(1);
                int pairPos = byIds.getInt(itemStack.getDescriptionId());
                if (pairPos == -1) {
                    int[] originalIndexs = new int[]{i};
                    this.pairList.add(Pair.of(originalIndexs, itemStack));
                    byIds.put(itemStack.getDescriptionId(), pairList.size() - 1);
                } else {
                    int[] originalIndexs = pairList.get(pairPos).first();
                    int[] originalIndexsNew = new int[originalIndexs.length + 1];
                    System.arraycopy(originalIndexs, 0, originalIndexsNew, 0, originalIndexs.length);
                    originalIndexsNew[originalIndexs.length] = i;
                    this.pairList.set(pairPos, Pair.of(originalIndexsNew, itemStack));
                }
            }
            i++;
        }
        // if(getMaxStartLine()<=0){
        //     startIndex=0;
        // }
        if (!isEmpty)
            startIndex = 0;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pGuiGraphics);
        renderLabels(pGuiGraphics, pMouseX, pMouseY);
        int lp = this.leftPos;
        int tp = this.topPos;
        pGuiGraphics.blit(BG_LOCATION, lp, tp, 0, 0, this.imageWidth, this.imageHeight);

        int l = this.leftPos + 52;
        int i1 = this.topPos + 14;
        int line_count = 4;

        int itemSelectIndexInReal = getItemSelectIndexInReal(pMouseX, pMouseY);
        for (int i = 0; i + startIndex < this.pairList.size() && i < 12; i++) {
            Pair<int[], ItemStack> stackPair = this.pairList.get(i + startIndex);
            int ih = this.imageHeight + 33;
            int pX = (i % line_count) * 16 + l;
            int pY = i / line_count * 18 + i1 + 1;
            if (itemSelectIndexInReal == stackPair.first()[0]) {
                pGuiGraphics.blit(BG_LOCATION, pX, pY, 0, ih - 33 + 18, 16, 18);
            } else {
                pGuiGraphics.blit(BG_LOCATION, pX, pY, 0, ih - 33, 16, 18);
            }
            // ItemStack stack = this.menu.itemStacks.get(i + startIndex);
            ItemStack stack = stackPair.value();
            pGuiGraphics.renderItem(stack, pX, pY + 1);
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0, 0, 200);
            // if (stack.getCount() > 1)
            {
                int count = 0;
                for (int i2 : stackPair.first()) {
                    count += this.menu.itemStacks.get(i2).getCount();
                }
                String countS;
                if (count > 99 * 1000000) {
                    countS = "99m";
                } else if (count > 999999) {
                    countS = count / 1000000 + "m";
                } else if (count > 999) {
                    countS = count / 1000 + "k";
                } else {
                    countS = Integer.toString(count);
                }
                float sc = 0.5f;
                float nsc = 1 / sc;
                pGuiGraphics.pose().scale(sc, sc, 0);

                pGuiGraphics.drawString(font, countS, (int) ((pX + 16) * nsc - font.width(countS)), (int) ((pY + 18) * nsc - font.lineHeight), Color.WHITE.getRGB());
            }
            pGuiGraphics.pose().popPose();
        }

        int k = (int) (41.0F * (Mth.clamp(startIndex / (getMaxStartLine() * 4f), 0f, 1f)));
        pGuiGraphics.blit(BG_LOCATION, lp + 119, tp + 15 + k, 176, 0, 12, 15);
        if (pairList.size() < 12) {
            pGuiGraphics.fill(lp + 119, tp + 15 + k, lp + 119 + 12, tp + 15 + 15, 0xccAAAAAA);
        }

        pGuiGraphics.fill(lp + 19, tp + 32, lp + 19 + 18, tp + 32 + 18, 0xbbC6C6C6);

        pGuiGraphics.fill(lp + 138, tp + 28, lp + 138 + 26, tp + 28 + 26, 0xbbC6C6C6);

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);

        this.nameEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (getFocused() != null && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey))
            return false;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }


    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        // this.menu.setCarried(Items.APPLE.getDefaultInstance());
        if (pMouseX > leftPos && pMouseX < leftPos + imageWidth
                && pMouseY > topPos && pMouseY < topPos + imageHeight) {
            if (pDelta > 0) {
                startIndex -= 4;
            } else {
                startIndex += 4;
            }
            startIndex = Mth.clamp(startIndex, 0, getMaxStartLine() * 4);
            return true;
        }
        // SafeReader.logger(pDelta, startIndex);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    public boolean isDragScroll(double pMouseX, double pMouseY) {
        int k = (int) (41.0F * (Mth.clamp(startIndex / (getMaxStartLine() * 4f), 0f, 1f)));
        int lp = this.leftPos;
        int tp = this.topPos;
        if (pMouseX >= lp + 119 && pMouseX <= lp + 119 + 12
                && pMouseY >= tp + 15 + k && pMouseY <= tp + 15 + 15 + k)
            return true;
        return false;
    }

    public boolean isInScroll(double pMouseX, double pMouseY) {
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isDragInScroll) {
            return mouseScrolled(pMouseX, pMouseY, -pDragY);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (isDragInScroll) {
            isDragInScroll = false;
            return true;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    public int getMaxStartLine() {
        // return Math.max(this.menu.itemStacks.size() / 4 - 2, 0);
        return Math.max((this.pairList.size() - 1) / 4 - 2, 0);
    }

    public int getItemSelectIndexInReal(double pMouseX, double pMouseY) {
        int bid = -1;
        int ls = this.leftPos + 52;
        int ts = this.topPos + 14;
        double u = ((pMouseX - ls) / 16f);
        double v = ((pMouseY - ts) / 18f);
        if (u >= 0 && u < 4
                && v >= 0 && v < 3) {
            {
                bid = (int) u + (int) v * 4 + startIndex;
                bid = bid < this.pairList.size() ? pairList.get(bid).first()[0] : -1;
            }
        }
        return bid;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (isDragScroll(pMouseX, pMouseY)) {
            isDragInScroll = true;
            return true;
        }
        int bid = getItemSelectIndexInReal(pMouseX, pMouseY);
        if (bid > -1) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, bid);
            this.menu.itemStacks.remove(bid);
            updateAddButtonStatus();
            if ((pairList.size()) % 4 == 0) {
                startIndex = startIndex > 4 ? startIndex - 4 : 0;
            }
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);

    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        int bid = getItemSelectIndexInReal(pX, pY);
        if (bid > -1) {
            pGuiGraphics.renderTooltip(font, this.menu.itemStacks.get(bid), pX, pY);
        }
    }
}
