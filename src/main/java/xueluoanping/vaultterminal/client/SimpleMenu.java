package xueluoanping.vaultterminal.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import xueluoanping.vaultterminal.ModContents;
import xueluoanping.vaultterminal.SafeReader;
import xueluoanping.vaultterminal.block.ReaderBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SimpleMenu extends AbstractContainerMenu {

    public static final int FLAG_ONCE = -1;
    public boolean shouldNextOneCount = false;

    private final ICapabilityProvider capabilityProvider;
    private final BlockPos blockPos;
    private final boolean isRemote;
    public List<ItemStack> itemStacks = new ArrayList<>();
    private List<Slot> playerSlots;

    private List<Slot> containerSlots;

    public SimpleMenu(int windowId, Inventory playerInv, @NotNull FriendlyByteBuf data) {
        this(ModContents.containerType.get(), windowId, playerInv, data.readBlockPos(), data.readEnum(Direction.class));
        int size = data.readVarInt();
        for (int i = 0; i < size; i++) {
            itemStacks.add(data.readItem());
        }
    }

    public SimpleMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory, BlockPos pos, Direction facing) {
        super(type, windowId);
        Level world = playerInventory.player.getCommandSenderWorld();
        this.blockPos = pos;
        int i;
        this.playerSlots = new ArrayList<>();
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.playerSlots.add(this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)));
            }
        }


        this.containerSlots = new ArrayList<>();
        for (int k = 0; k < 9; ++k) {
            this.containerSlots.add(this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142)));
        }

        BlockEntity blockEntity1;
        try {
            blockEntity1 = playerInventory.player.level().getBlockEntity(pos.relative(facing.getOpposite()));
        } catch (Exception e) {
            blockEntity1 = null;
        }
        this.capabilityProvider = blockEntity1;


        // TODO:这段代码是因为机械动力的保险箱不同步数据到客户端
        //  因此需要那边同步，但是这里的话，有些容器会同步，因此要注意，后续清理一下即可
        if (capabilityProvider != null && !world.isClientSide()) {
            LazyOptional<IItemHandler> capability = capabilityProvider.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (capability.resolve().isPresent()) {
                List<ItemStack> itemStacks = new ArrayList<>();
                IItemHandler iItemHandler = capability.resolve().get();
                for (int j = 0; j < iItemHandler.getSlots(); j++) {
                    ItemStack stackInSlot = iItemHandler.getStackInSlot(j);
                    if (!stackInSlot.isEmpty()) {
                        itemStacks.add(stackInSlot);
                    }
                }
                this.itemStacks.addAll(itemStacks);
            }
        }

        this.isRemote = world.isClientSide();

    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    private boolean validCapabilityProvider() {
        return capabilityProvider != null
                && capabilityProvider.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().isPresent();
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        // TODO: 比较物品是否发生变化
        return validCapabilityProvider()
                && pPlayer.blockPosition().getCenter().distanceToSqr(blockPos.getCenter()) <= 48;
    }

    private boolean isSameItem(@NotNull ItemStack stack, @NotNull ItemStack stackB) {
        return stackB.is(stack.getItem())
                && ((stackB.getTag() == null && stack.getTag() == null) ||
                (stackB.getTag() != null && stackB.getTag().equals(stack.getTag())));
    }

    private int extractItem(IItemHandler itemHandler, ItemStack stack, int count, boolean simulate) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (count <= 0) break;
            if (isSameItem(stack, itemHandler.getStackInSlot(i))) {
                ItemStack extractItem = itemHandler.extractItem(i, count, simulate);
                count -= extractItem.getCount();
            }
        }
        return count;
    }


    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        // if (pId == FLAG_ONCE) {
        //     shouldNextOneCount = true;
        //     return false;
        // }
        SafeReader.logger(pPlayer,shouldNextOneCount);
        if (pId > -1 && pId < itemStacks.size()
                && validCapabilityProvider()) {
            boolean oneCount = shouldNextOneCount;
            shouldNextOneCount = false;
            ItemStack stack = itemStacks.get(pId);
            IItemHandler iItemHandler = capabilityProvider.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

            int count = oneCount ? 1 : stack.getCount();

            int remainCount = extractItem(iItemHandler, stack, count, true);
            if (remainCount >= count) return false;
            remainCount = extractItem(iItemHandler, stack, count, false);
            if (remainCount >= count) return false;
            // setCarried(itemStacks.get(pId));

            ItemHandlerHelper.giveItemToPlayer(pPlayer, stack.copyWithCount(count - remainCount));
            if (oneCount && itemStacks.get(pId).getCount() > 1) {
                itemStacks.get(pId).setCount(itemStacks.get(pId).getCount() - 1);
            } else itemStacks.remove(pId);

            return true;
        }
        return super.clickMenuButton(pPlayer, pId);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        if (pPlayer.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (blockState.getBlock() instanceof ReaderBlock) {
                serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(ReaderBlock.OPEN, false));
            }
        }
    }
}
