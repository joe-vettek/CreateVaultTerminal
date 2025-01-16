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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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

    private final BlockEntity blockEntity;
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
        this.blockEntity = blockEntity1;


        // TODO:这段代码是因为机械动力的保险箱不同步数据到客户端
        //  因此需要那边同步，但是这里的话，有些容器会同步，因此要注意，后续清理一下即可
        if (blockEntity != null && !world.isClientSide()) {
            LazyOptional<IItemHandler> capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
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

    public static BlockEntity getTileEntity(Inventory playerInv, BlockPos pos) {
        BlockEntity blockEntity = playerInv.player.getCommandSenderWorld().getBlockEntity(pos);
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        // TODO: 比较物品是否发生变化
        return blockEntity != null
                && pPlayer.blockPosition().getCenter().distanceToSqr(blockPos.getCenter()) <= 48
                && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId > -1 && pId < itemStacks.size()
                && blockEntity != null
                && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().isPresent()) {
            ItemStack stack = itemStacks.get(pId);
            IItemHandler iItemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

            int count = stack.getCount();
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                if (count <= 0) break;
                ItemStack stackInSlot = iItemHandler.getStackInSlot(i);
                if (stackInSlot.is(stack.getItem())
                        && (
                        (stackInSlot.getTag() == null && stack.getTag() == null) ||
                                (stackInSlot.getTag() != null && stackInSlot.getTag().equals(stack.getTag())))) {
                    ItemStack stack1 = iItemHandler.extractItem(i, count, true);
                    count -= stack1.getCount();
                }
            }
            if (count > 0) return false;

            count = stack.getCount();
            for (int i = 0; i < iItemHandler.getSlots(); i++) {
                if (count <= 0) break;
                ItemStack stackInSlot = iItemHandler.getStackInSlot(i);
                if (stackInSlot.is(stack.getItem())
                        && (
                        (stackInSlot.getTag() == null && stack.getTag() == null) ||
                                (stackInSlot.getTag() != null && stackInSlot.getTag().equals(stack.getTag())))) {
                    ItemStack stack1 = iItemHandler.extractItem(i, count, false);
                    count -= stack1.getCount();
                }
            }

            // setCarried(itemStacks.get(pId));

            ItemHandlerHelper.giveItemToPlayer(pPlayer, stack);

            // 这里好像有bug
            itemStacks.remove(pId);

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
