package xueluoanping.vaultterminal.client;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import xueluoanping.vaultterminal.VaultTerminal;
import xueluoanping.vaultterminal.network.GiveItemMessage;
import xueluoanping.vaultterminal.network.NetworkUtil;
import xueluoanping.vaultterminal.network.SimpleNetworkHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class SimpleScreen extends AbstractContainerScreen<SimpleMenu> {

    public static ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");

    private int startIndex = 0;
    private EditBox nameEdit;

    private final List<Pair<int[], ItemStack>> itemsHolder = new ArrayList<>();

    private boolean isDragInScroll = false;
    private boolean isShiftPressed = false;

    public static final int MAX_ITEMS_IN_LINE = 4;
    public static final int MAX_LINES_SHOWN = 3;
    public static final int MAX_ITEMS_SHOWN = MAX_ITEMS_IN_LINE * MAX_LINES_SHOWN;


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
            this.updateSearchStatus();
        });
        this.addWidget(this.nameEdit);
        // init Status
        updateSearchStatus();
    }

    private void updateSearchStatus() {
        String name = nameEdit.getValue();
        // this.itemsHolder.clear();
        List<Pair<int[], ItemStack>> newItemsHolder = new ArrayList<>();
        boolean isEmpty = name.isEmpty();
        String nameUse = name.toLowerCase(Locale.ROOT);
        Object2IntArrayMap<String> byIds = new Object2IntArrayMap<>();
        byIds.defaultReturnValue(-1);
        int i = 0;
        int count = 0;
        for (ItemStack itemStack : this.menu.itemStacks) {
            // do not think about the empty item
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
            // if (itemStack.isEmpty()) {
            //     should = false;
            // }
            if (should) {
                count += itemStack.getCount();
                itemStack = itemStack.copyWithCount(1);
                String itemStackString = itemStack.getDescriptionId() + " " + itemStack.getTag();
                int pairPos = byIds.getInt(itemStackString);
                if (pairPos == -1) {
                    int[] originalIndexs = new int[]{i};
                    newItemsHolder.add(Pair.of(originalIndexs, itemStack));
                    byIds.put(itemStackString, newItemsHolder.size() - 1);
                } else {
                    int[] originalIndexs = newItemsHolder.get(pairPos).first();
                    int[] originalIndexsNew = new int[originalIndexs.length + 1];
                    System.arraycopy(originalIndexs, 0, originalIndexsNew, 0, originalIndexs.length);
                    originalIndexsNew[originalIndexs.length] = i;
                    newItemsHolder.set(pairPos, Pair.of(originalIndexsNew, itemStack));
                }
            }
            i++;
        }
        byIds.clear();

        // long l = System.currentTimeMillis();
        if (!this.itemsHolder.isEmpty()) {
            newItemsHolder.sort(Comparator.comparing(p ->
                    {
                        int result = -1;
                        for (int i1 = 0; i1 < itemsHolder.size(); i1++) {
                            Pair<int[], ItemStack> stackPair = itemsHolder.get(i1);
                            if (p.second().equals(stackPair.second(), false)) {
                                result = i1;
                                break;
                            }
                        }
                        return result;
                    }

            ));
        }
        // VaultTerminal.logger(System.currentTimeMillis()-l);
        this.itemsHolder.clear();
        this.itemsHolder.addAll(newItemsHolder);
        // if(getMaxStartLine()<=0){
        //     startIndex=0;
        // }
        if (!isEmpty) {
            startIndex = 0;
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pGuiGraphics);
        int lp = this.leftPos;
        int tp = this.topPos;
        pGuiGraphics.blit(BG_LOCATION, lp, tp, 0, 0, this.imageWidth, this.imageHeight);

        int l = this.leftPos + 52;
        int i1 = this.topPos + 14;

        int itemSelectIndexInReal = getItemSelectIndexInReal(pMouseX, pMouseY);
        for (int i = 0; i + startIndex < this.itemsHolder.size() && i < MAX_ITEMS_SHOWN; i++) {
            int nowI = i + startIndex;
            Pair<int[], ItemStack> stackPair = this.itemsHolder.get(nowI);
            int ih = this.imageHeight + 33;
            int pX = (i % MAX_ITEMS_IN_LINE) * 16 + l;
            int pY = i / MAX_ITEMS_IN_LINE * 18 + i1 + 1;
            if (itemSelectIndexInReal == nowI) {
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
                if (count > 1) {
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
            }
            pGuiGraphics.pose().popPose();
        }

        int k = (int) (41.0F * (Mth.clamp(startIndex / (getMaxStartLine() * 4f), 0f, 1f)));
        pGuiGraphics.blit(BG_LOCATION, lp + 119, tp + 15 + k, 176, 0, 12, 15);
        if (itemsHolder.size() < 12) {
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
        if (mouseKey.getName().endsWith("shift"))
            isShiftPressed = true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (mouseKey.getName().endsWith("shift"))
            isShiftPressed = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        // this.menu.setCarried(Items.APPLE.getDefaultInstance());
        if (pMouseX > leftPos && pMouseX < leftPos + imageWidth
                && pMouseY > topPos && pMouseY < topPos + imageHeight) {
            if (pDelta > 0) {
                startIndex -= MAX_ITEMS_IN_LINE;
            } else {
                startIndex += MAX_ITEMS_IN_LINE;
            }
            startIndex = Mth.clamp(startIndex, 0, getMaxStartLine() * MAX_ITEMS_IN_LINE);
            return true;
        }
        // SafeReader.logger(pDelta, startIndex);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    public boolean isDragScroll(double pMouseX, double pMouseY) {
        int k = (int) (41.0F * (Mth.clamp(startIndex / (getMaxStartLine() * 4f), 0f, 1f)));
        int lp = this.leftPos;
        int tp = this.topPos;
        return pMouseX >= lp + 119 && pMouseX <= lp + 119 + 12
                && pMouseY >= tp + 15 + k && pMouseY <= tp + 15 + 15 + k;
    }


    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isDragInScroll && Math.abs(pDragY) > 0.2f) {
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
        return Math.max((this.itemsHolder.size() - 1) / MAX_ITEMS_IN_LINE - (MAX_LINES_SHOWN - 1), 0);
    }

    public int getItemSelectIndexInReal(double pMouseX, double pMouseY) {
        int ids = -1;
        int ls = this.leftPos + 52;
        int ts = this.topPos + 14;
        double u = ((pMouseX - ls) / 16f);
        double v = ((pMouseY - ts) / 18f);
        if (u >= 0 && u < MAX_ITEMS_IN_LINE
                && v >= 0 && v < MAX_LINES_SHOWN) {
            {
                int bid = (int) u + (int) v * MAX_ITEMS_IN_LINE + startIndex;
                ids = bid < this.itemsHolder.size() ? bid : -1;
                // ids = bid < this.itemsHolder.size() ? itemsHolder.get(bid).first() : new int[]{};
            }
        }
        return ids;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (isDragScroll(pMouseX, pMouseY)) {
            isDragInScroll = true;
            return true;
        }
        int ids = getItemSelectIndexInReal(pMouseX, pMouseY);
        if (ids > -1) {
            // int[] setIds = isShiftPressed ? ids : new int[]{ids[0]};
            boolean isRemove = false;

            Pair<int[], ItemStack> pair = this.itemsHolder.get(ids);
            int[] ints = pair.first();
            int bid = ints[0];
            SimpleNetworkHandler.send(new GiveItemMessage((this.menu).containerId, bid, isShiftPressed));

            ItemStack stack = this.menu.itemStacks.get(bid);
            int outCount = !isShiftPressed ? 1 : Math.min(stack.getCount(), stack.getMaxStackSize());
            if (stack.getCount() > outCount) {
                stack.setCount(stack.getCount() - outCount);
            } else {
                this.menu.itemStacks.remove(bid);
                isRemove = true;
            }
            if (isRemove) {
                // all id is wrong
                updateSearchStatus();
                // if (ids % MAX_ITEMS_IN_LINE == 3
                //         && ids + MAX_ITEMS_SHOWN >= itemsHolder.size()
                //         && itemsHolder.size() % MAX_ITEMS_IN_LINE == 3) {
                //     startIndex = startIndex > MAX_ITEMS_IN_LINE ?
                //             startIndex - MAX_ITEMS_IN_LINE : 0;
                // }
                //
                startIndex = Mth.clamp(startIndex, 0, getMaxStartLine() * MAX_ITEMS_IN_LINE);
            }
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);

    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics pGuiGraphics, int pX, int pY) {
        int ids = getItemSelectIndexInReal(pX, pY);
        if (ids > -1) {
            pGuiGraphics.renderTooltip(font, this.menu.itemStacks.get(this.itemsHolder.get(ids).first()[0]), pX, pY);
        }

        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}
